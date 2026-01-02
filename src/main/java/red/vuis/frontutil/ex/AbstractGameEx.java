package red.vuis.frontutil.ex;

import red.vuis.frontutil.client.data.config.AddonClientConfig;

public interface AbstractGameEx {
	boolean frontutil$isForceClientConfig();
	
	AddonClientConfig.Data frontutil$getClientConfig();
}
