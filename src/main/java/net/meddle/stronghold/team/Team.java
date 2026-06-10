package net.meddle.stronghold.team;

import org.bukkit.DyeColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Team {

    private String name;
    private DyeColor color;
    private UUID captain;
    private final List<UUID> members = new ArrayList<>();
    private final Map<UUID, String> memberNames = new HashMap<>();

    private String vaultWorld;
    private int vaultX, vaultY, vaultZ;
    private boolean vaultSet = false;
    private boolean vaultLocked = false;

    public Team(String name, DyeColor color) {
        this.name  = name;
        this.color = color;
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    public String getName()           { return name; }
    public void   setName(String n)   { this.name = n; }

    public DyeColor getColor()              { return color; }
    public void     setColor(DyeColor c)    { this.color = c; }

    // ── Captain ───────────────────────────────────────────────────────────────

    public UUID getCaptain()           { return captain; }
    public void setCaptain(UUID uuid)  { this.captain = uuid; }
    public boolean isCaptain(UUID uuid){ return uuid != null && uuid.equals(captain); }

    // ── Members ───────────────────────────────────────────────────────────────

    public List<UUID> getMembers()          { return members; }
    public boolean hasMember(UUID uuid)     { return members.contains(uuid); }
    public void addMember(UUID uuid)        { if (!members.contains(uuid)) members.add(uuid); }
    public void removeMember(UUID uuid)     {
        members.remove(uuid);
        memberNames.remove(uuid);
        if (uuid.equals(captain)) captain = null;
    }

    public void   setMemberName(UUID uuid, String name) { if (name != null) memberNames.put(uuid, name); }
    public String getMemberName(UUID uuid)               { return memberNames.get(uuid); }
    public Map<UUID, String> getMemberNames()            { return Collections.unmodifiableMap(memberNames); }

    // ── Vault ─────────────────────────────────────────────────────────────────

    public boolean isVaultSet()    { return vaultSet; }
    public boolean isVaultLocked() { return vaultLocked; }
    public void    lockVault()     { this.vaultLocked = true; }

    public String getVaultWorld()  { return vaultWorld; }
    public int    getVaultX()      { return vaultX; }
    public int    getVaultY()      { return vaultY; }
    public int    getVaultZ()      { return vaultZ; }

    public void setVault(Location loc) {
        this.vaultWorld = loc.getWorld().getName();
        this.vaultX     = loc.getBlockX();
        this.vaultY     = loc.getBlockY();
        this.vaultZ     = loc.getBlockZ();
        this.vaultSet   = true;
    }

    public void setVaultRaw(String world, int x, int y, int z) {
        this.vaultWorld = world;
        this.vaultX = x;
        this.vaultY = y;
        this.vaultZ = z;
        this.vaultSet = (world != null);
    }

    public void setVaultLocked(boolean locked) { this.vaultLocked = locked; }

    public Location getVaultLocation() {
        if (!vaultSet) return null;
        var w = org.bukkit.Bukkit.getWorld(vaultWorld);
        if (w == null) return null;
        return new Location(w, vaultX, vaultY, vaultZ);
    }

    /** Checks whether the given block coordinates match this vault. */
    public boolean isVaultAt(String world, int x, int y, int z) {
        return vaultSet
            && vaultWorld.equals(world)
            && vaultX == x && vaultY == y && vaultZ == z;
    }
}
