package org.bookmap;

import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.Log;

@Layer1Attachable
@Layer1StrategyName("Ilbko's Weis Wave")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class IlbkoWeisWave implements Layer1ApiFinishable {
    public IlbkoWeisWave(Layer1ApiProvider provider) {
        super();
    }

    @Override
    public void finish() {
        Log.info("Unloaded");
    }
}
