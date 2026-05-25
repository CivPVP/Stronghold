package me.neznamy.tab.libs.redis.clients.jedis;

@Deprecated
public abstract class PipelineBase extends AbstractPipeline {
   protected PipelineBase(CommandObjects commandObjects) {
      super(commandObjects);
   }
}
