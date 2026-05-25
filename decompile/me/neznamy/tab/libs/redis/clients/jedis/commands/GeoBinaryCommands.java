package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.GeoCoordinate;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusStoreParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoSearchParam;
import me.neznamy.tab.libs.redis.clients.jedis.resps.GeoRadiusResponse;

public interface GeoBinaryCommands {
   long geoadd(byte[] var1, double var2, double var4, byte[] var6);

   long geoadd(byte[] var1, Map<byte[], GeoCoordinate> var2);

   long geoadd(byte[] var1, GeoAddParams var2, Map<byte[], GeoCoordinate> var3);

   Double geodist(byte[] var1, byte[] var2, byte[] var3);

   Double geodist(byte[] var1, byte[] var2, byte[] var3, GeoUnit var4);

   List<byte[]> geohash(byte[] var1, byte[]... var2);

   List<GeoCoordinate> geopos(byte[] var1, byte[]... var2);

   List<GeoRadiusResponse> georadius(byte[] var1, double var2, double var4, double var6, GeoUnit var8);

   List<GeoRadiusResponse> georadiusReadonly(byte[] var1, double var2, double var4, double var6, GeoUnit var8);

   List<GeoRadiusResponse> georadius(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   List<GeoRadiusResponse> georadiusReadonly(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   List<GeoRadiusResponse> georadiusByMember(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   List<GeoRadiusResponse> georadiusByMember(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   long georadiusStore(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9, GeoRadiusStoreParam var10);

   long georadiusByMemberStore(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6, GeoRadiusStoreParam var7);

   List<GeoRadiusResponse> geosearch(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   List<GeoRadiusResponse> geosearch(byte[] var1, GeoCoordinate var2, double var3, GeoUnit var5);

   List<GeoRadiusResponse> geosearch(byte[] var1, byte[] var2, double var3, double var5, GeoUnit var7);

   List<GeoRadiusResponse> geosearch(byte[] var1, GeoCoordinate var2, double var3, double var5, GeoUnit var7);

   List<GeoRadiusResponse> geosearch(byte[] var1, GeoSearchParam var2);

   long geosearchStore(byte[] var1, byte[] var2, byte[] var3, double var4, GeoUnit var6);

   long geosearchStore(byte[] var1, byte[] var2, GeoCoordinate var3, double var4, GeoUnit var6);

   long geosearchStore(byte[] var1, byte[] var2, byte[] var3, double var4, double var6, GeoUnit var8);

   long geosearchStore(byte[] var1, byte[] var2, GeoCoordinate var3, double var4, double var6, GeoUnit var8);

   long geosearchStore(byte[] var1, byte[] var2, GeoSearchParam var3);

   long geosearchStoreStoreDist(byte[] var1, byte[] var2, GeoSearchParam var3);
}
