package red.vuis.frontutil.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.boehmod.blockfront.util.math.FDSPose;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.client.widget.BoolButtonWidget;
import red.vuis.frontutil.client.widget.DoubleFieldWidget;
import red.vuis.frontutil.client.widget.FloatFieldWidget;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.data.edit.GameEditData;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.dim;

public class GameEditorScreen extends AddonScreen {
	private static final Text C_LABEL_ROTATION = Text.translatable("frontutil.screen.game.editor.label.rotation");
	private static final Text C_LABEL_TEAM_SPAWNS = Text.translatable("frontutil.screen.game.editor.label.teamSpawns");
	
	private final GameEditData editData;
	
	private BoolButtonWidget rotationButton;
	
	public GameEditorScreen(String gameName, GameEditData editData) {
		super(Text.translatable("frontutil.screen.game.editor.header", gameName));
		this.editData = editData;
	}
	
	@Override
	protected void init() {
		super.init();
		
		rotationButton = addDrawableChild(new BoolButtonWidget(
			editData.isInRotation(),
			dim(width / 2, 60, 100, 20)
		));
		addDrawableChild(Widgets.button(
			C_LABEL_TEAM_SPAWNS,
			dim(width / 2 - 100, 80, 200, 20),
			button -> setScreen(new SelectTeamScreen())
		));
		
		addDrawableChild(Widgets.button(
			ScreenTexts.DONE,
			centeredDim(width / 2, height - 20, 90, 20),
			button -> close()
		));
	}
	
	@Override
	public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		drawText(title, width / 2, 20, true);
		
		drawRect(width / 2 - 100, 60, 100, 20, 0x7F000000);
		drawText(C_LABEL_ROTATION, width / 2 - 50, 70, true);
	}
	
	private class SelectTeamScreen extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.game.editor.selectTeam.header");
		
		private SelectTeamScreen() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			int y = 60;
			for (String teamName : editData.getTeamSpawns().keySet()) {
				addDrawableChild(Widgets.button(
					Text.literal(teamName),
					dim(width / 2 - 100, y, 200, 20),
					button -> setScreen(new TeamSpawnsScreen(teamName))
				));
				y += 20;
			}
			
			addDrawableChild(Widgets.button(
				ScreenTexts.BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> setScreen(GameEditorScreen.this)
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(GameEditorScreen.this.title, width / 2, 20, true);
			
			drawText(title, width / 2, 40, true);
		}
	}
	
	private class TeamSpawnsScreen extends AddonScreen {
		private static final Text C_LABEL_REMOVE = Text.literal("-");
		private static final Text C_LABEL_X = Text.literal("X");
		private static final Text C_LABEL_Y = Text.literal("Y");
		private static final Text C_LABEL_Z = Text.literal("Z");
		private static final Text C_LABEL_PITCH = Text.translatable("frontutil.screen.generic.label.pitch");
		private static final Text C_LABEL_YAW = Text.translatable("frontutil.screen.generic.label.yaw");
		
		private final List<FDSPose> spawns;
		private final List<EntryWidgetHolder> widgetEntries;
		
		private TeamSpawnsScreen(String teamName) {
			super(Text.translatable("frontutil.screen.game.editor.teamSpawns.header", teamName));
			this.spawns = editData.getTeamSpawns().get(teamName);
			this.widgetEntries = new ArrayList<>(spawns.size());
		}
		
		@Override
		protected void init() {
			super.init();
			
			spawns.forEach(this::addEntry);
			
			addDrawableChild(Widgets.button(
				ScreenTexts.BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> setScreen(GameEditorScreen.this)
			));
		}
		
		private void addEntry(FDSPose spawn) {
			int y = 60 + widgetEntries.size() * 20;
			
			ButtonWidget removeButton = addDrawableChild(Widgets.button(
				C_LABEL_REMOVE,
				dim(width / 2 - 155, y, 10, 20),
				button -> {}
			));
			
			DoubleFieldWidget xField = addDrawableChild(new DoubleFieldWidget(
				textRenderer,
				dim(width / 2 - 145, y, 60, 20),
				C_LABEL_X
			));
			xField.setDouble(spawn.position.x);
			
			DoubleFieldWidget yField = addDrawableChild(new DoubleFieldWidget(
				textRenderer,
				dim(width / 2 - 85, y, 60, 20),
				C_LABEL_Y
			));
			yField.setDouble(spawn.position.y);
			
			DoubleFieldWidget zField = addDrawableChild(new DoubleFieldWidget(
				textRenderer,
				dim(width / 2 - 25, y, 60, 20),
				C_LABEL_Z
			));
			zField.setDouble(spawn.position.z);
			
			FloatFieldWidget pitchField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				dim(width / 2 + 35, y, 60, 20),
				C_LABEL_PITCH
			));
			pitchField.setFloat(spawn.rotation.x);
			
			FloatFieldWidget yawField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				dim(width / 2 + 95, y, 60, 20),
				C_LABEL_YAW
			));
			yawField.setFloat(spawn.rotation.y);
			
			widgetEntries.add(new EntryWidgetHolder(
				removeButton,
				xField,
				yField,
				zField,
				pitchField,
				yawField
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(GameEditorScreen.this.title, width / 2, 20, true);
			
			drawText(title, width / 2, 40, true);
		}
		
		private record EntryWidgetHolder(
			ButtonWidget remove,
			DoubleFieldWidget x,
			DoubleFieldWidget y,
			DoubleFieldWidget z,
			FloatFieldWidget pitch,
			FloatFieldWidget yaw
		) {
		}
	}
}
