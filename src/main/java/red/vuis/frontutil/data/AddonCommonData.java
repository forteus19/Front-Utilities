package red.vuis.frontutil.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.io.IOUtils;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.net.packet.SetProfileOverridesPacket;
import red.vuis.frontutil.server.data.config.AddonServerConfig;
import red.vuis.frontutil.util.AddonHttpUtils;
import red.vuis.frontutil.util.AddonUtils;

public final class AddonCommonData {
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static AddonCommonData instance = null;
	
	public Map<UUID, PlayerCloudData> profileOverrides = new ConcurrentHashMap<>();
	
	private AddonCommonData() {
	}
	
	public static AddonCommonData getInstance() {
		if (instance == null) {
			instance = new AddonCommonData();
		}
		return instance;
	}
	
	public void syncProfileOverrides(Set<UUID> uuids) {
		PacketDistributor.sendToAllPlayers(new SetProfileOverridesPacket(
			profileOverrides.entrySet().stream().filter(e -> uuids.contains(e.getKey())).collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> ProfileOverrideData.of(e.getValue())
			))
		));
	}
	
	public void putNewProfileOverride(Pair<UUID, String> idPair) {
		profileOverrides.put(idPair.left(), AddonUtils.createPlayerCloudData(idPair));
	}
	
	public void putNewProfileOverrides(Collection<Pair<UUID, String>> idPairs) {
		idPairs.forEach(this::putNewProfileOverride);
	}
	
	public Map<UUID, ProfileOverrideData> getProfileOverrideData() {
		Map<UUID, ProfileOverrideData> data = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<UUID, PlayerCloudData> entry : profileOverrides.entrySet()) {
			data.put(entry.getKey(), ProfileOverrideData.of(entry.getValue()));
		}
		return data;
	}
	
	public void fetchCloudProfiles(Set<UUID> uuids) {
		if (uuids.isEmpty()) {
			throw new IllegalArgumentException("Empty UUID set");
		}
		
		new Thread(() -> fetchCloudProfilesThread(uuids), "Profile fetch").start();
	}
	
	private void fetchCloudProfilesThread(Set<UUID> uuids) {
		if (uuids.size() == 1) {
			UUID uuid = uuids.iterator().next();
			
			HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(AddonServerConfig.getProfileFetchHost() + "/api/v1/player_data?uuid=" + uuid.toString()))
				.build();
			
			HttpResponse<InputStream> response;
			try {
				response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (InterruptedException | IOException e) {
				AddonConstants.LOGGER.error("Exception while fetching profile for {}", uuid);
				AddonConstants.LOGGER.throwing(e);
				return;
			}
			
			if (!AddonHttpUtils.isStatusOk(response.statusCode())) {
				try {
					AddonConstants.LOGGER.error("Failed to fetch profile for {} ({})\n{}", uuid, response.statusCode(), IOUtils.toString(response.body(), StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return;
			}
			
			JsonObject json;
			try {
				json = JsonParser.parseReader(AddonUtils.toBufferedReader(response.body())).getAsJsonObject();
			} catch (Exception e) {
				AddonConstants.LOGGER.error("Failed to parse json for profile {}", uuid);
				AddonConstants.LOGGER.throwing(e);
				return;
			}
			
			handleProfileJson(uuid, json);
			
			AddonConstants.LOGGER.info("Successfully fetched profile for {}", uuid);
		} else {
			HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(uuids.stream().map(UUID::toString).collect(Collectors.joining(","))))
				.uri(URI.create(AddonServerConfig.getProfileFetchHost() + "/api/v1/player_data/bulk"))
				.build();
			
			HttpResponse<InputStream> response;
			try {
				response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (InterruptedException | IOException e) {
				AddonConstants.LOGGER.error("Exception while bulk fetching profile for {} players", uuids.size());
				AddonConstants.LOGGER.throwing(e);
				return;
			}
			
			if (!AddonHttpUtils.isStatusOk(response.statusCode())) {
				try {
					AddonConstants.LOGGER.error("Failed to bulk fetch profile for {} players ({})\n{}", uuids.size(), response.statusCode(), IOUtils.toString(response.body(), StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return;
			}
			
			Map<UUID, JsonObject> profileJsonMap = new Object2ObjectOpenHashMap<>(uuids.size());
			try {
				JsonArray json = JsonParser.parseReader(AddonUtils.toBufferedReader(response.body())).getAsJsonArray();
				for (JsonElement profileElement : json) {
					JsonObject profileObject = profileElement.getAsJsonObject();
					profileJsonMap.put(UUID.fromString(profileObject.get("uuid").getAsString()), profileObject);
				}
			} catch (Exception e) {
				AddonConstants.LOGGER.error("Failed to parse json for bulk profile fetch", e);
				return;
			}
			
			profileJsonMap.forEach(this::handleProfileJson);
			
			AddonConstants.LOGGER.info("Successfully fetched profiles for {} players", uuids.size());
		}
		
		if (FMLEnvironment.dist.isDedicatedServer()) {
			syncProfileOverrides(uuids);
		}
	}
	
	private void handleProfileJson(UUID uuid, JsonObject json) {
		ProfileOverrideData overrideData;
		try {
			overrideData = ProfileOverrideData.of(json);
		} catch (Exception e) {
			AddonConstants.LOGGER.error("Failed to handle json for profile {}", uuid);
			AddonConstants.LOGGER.throwing(e);
			return;
		}
		
		PlayerCloudData cloudData = new PlayerCloudData(uuid);
		overrideData.apply(cloudData);
		profileOverrides.put(uuid, cloudData);
	}
}
