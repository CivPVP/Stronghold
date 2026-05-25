package me.neznamy.tab.shared.cpu;

import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CpuManager {
   private final int UPDATE_RATE_SECONDS = 10;
   private volatile Map<String, Map<String, AtomicLong>> featureUsageCurrent = new ConcurrentHashMap<>();
   private volatile Map<String, AtomicLong> placeholderUsageCurrent = new ConcurrentHashMap<>();
   @Nullable
   private CpuReport lastReport;
   private final ThreadExecutor processingThread = new ThreadExecutor("TAB Processing Thread");
   private final ThreadExecutor placeholderThread = new ThreadExecutor("TAB Placeholder Refreshing Thread");
   private final ThreadExecutor groupRefreshingThread = new ThreadExecutor("TAB Permission Group Refreshing Thread");
   private final ThreadExecutor tablistEntryCheckThread = new ThreadExecutor("TAB TabList Entry Checker Thread");
   private static final ThreadExecutor pluginMessageEncodeThread = new ThreadExecutor("TAB Plugin Message Encoding Thread");
   private final ThreadExecutor pluginMessageDecodeThread = new ThreadExecutor("TAB Plugin Message Decoding Thread");
   private final ThreadExecutor mysqlThread = new ThreadExecutor("TAB MySQL Thread");
   private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
   private volatile boolean enabled;
   private boolean trackUsage;

   public boolean enableTracking() {
      if (this.trackUsage) {
         return false;
      }

      this.trackUsage = true;
      this.processingThread.repeatTask(new TimedCaughtTask(this, () -> {
         this.lastReport = new CpuReport(10, this.featureUsageCurrent, this.placeholderUsageCurrent);
         this.featureUsageCurrent = new ConcurrentHashMap<>();
         this.placeholderUsageCurrent = new ConcurrentHashMap<>();
      }, "CPU Tracking", "Resetting values"), (int)TimeUnit.SECONDS.toMillis(10L));
      return true;
   }

   public void cancelAllTasks() {
      this.processingThread.shutdown();
      this.placeholderThread.shutdown();
      this.groupRefreshingThread.shutdown();
      this.tablistEntryCheckThread.shutdown();
      this.pluginMessageDecodeThread.shutdown();
      this.mysqlThread.shutdown();
   }

   public void enable() {
      this.enabled = true;

      Runnable r;
      while ((r = this.taskQueue.poll()) != null) {
         this.submit(r);
      }
   }

   private void submit(@NotNull Runnable task) {
      if (!this.enabled) {
         this.taskQueue.add(task);
      } else {
         this.processingThread.execute(task);
      }
   }

   public void addTime(@NotNull String feature, @NotNull String type, long nanoseconds) {
      if (this.trackUsage) {
         this.featureUsageCurrent.computeIfAbsent(feature, f -> new ConcurrentHashMap<>()).computeIfAbsent(type, t -> new AtomicLong()).addAndGet(nanoseconds);
      }
   }

   public void addPlaceholderTime(@NotNull String placeholder, long nanoseconds) {
      if (this.trackUsage) {
         this.placeholderUsageCurrent.computeIfAbsent(placeholder, l -> new AtomicLong()).addAndGet(nanoseconds);
      }
   }

   public void addPlaceholderTimes(@NotNull Map<String, Long> times) {
      if (this.trackUsage) {
         for (Entry<String, Long> entry : times.entrySet()) {
            this.placeholderUsageCurrent.computeIfAbsent(entry.getKey(), l -> new AtomicLong()).addAndGet(entry.getValue());
         }
      }
   }

   public void runMeasuredTask(@NotNull String feature, @NotNull String type, @NotNull Runnable task) {
      if (!this.enabled) {
         this.taskQueue.add(task);
      } else {
         this.processingThread.execute(new TimedCaughtTask(this, task, feature, type));
      }
   }

   public void runTask(@NotNull Runnable task) {
      this.submit(task);
   }

   @Generated
   public int getUPDATE_RATE_SECONDS() {
      return 10;
   }

   @Generated
   public Map<String, Map<String, AtomicLong>> getFeatureUsageCurrent() {
      return this.featureUsageCurrent;
   }

   @Generated
   public Map<String, AtomicLong> getPlaceholderUsageCurrent() {
      return this.placeholderUsageCurrent;
   }

   @Nullable
   @Generated
   public CpuReport getLastReport() {
      return this.lastReport;
   }

   @Generated
   public ThreadExecutor getProcessingThread() {
      return this.processingThread;
   }

   @Generated
   public ThreadExecutor getPlaceholderThread() {
      return this.placeholderThread;
   }

   @Generated
   public ThreadExecutor getGroupRefreshingThread() {
      return this.groupRefreshingThread;
   }

   @Generated
   public ThreadExecutor getTablistEntryCheckThread() {
      return this.tablistEntryCheckThread;
   }

   @Generated
   public ThreadExecutor getPluginMessageDecodeThread() {
      return this.pluginMessageDecodeThread;
   }

   @Generated
   public ThreadExecutor getMysqlThread() {
      return this.mysqlThread;
   }

   @Generated
   public Queue<Runnable> getTaskQueue() {
      return this.taskQueue;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isTrackUsage() {
      return this.trackUsage;
   }

   @Generated
   public static ThreadExecutor getPluginMessageEncodeThread() {
      return pluginMessageEncodeThread;
   }
}
