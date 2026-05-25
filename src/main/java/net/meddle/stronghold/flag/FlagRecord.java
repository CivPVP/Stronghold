package net.meddle.stronghold.flag;

import java.util.UUID;

public class FlagRecord {

    private final String ownerTeam;
    private final UUID flagUuid;

    private FlagState state = FlagState.IN_VAULT;

    private UUID   holderUUID;
    private String holderName;

    private String droppedWorld;
    private double droppedX, droppedY, droppedZ;
    private UUID   droppedEntityUUID;

    /** The team name whose vault the flag was last physically stored in. */
    private String lastVaultTeam;

    public FlagRecord(String ownerTeam, UUID flagUuid) {
        this.ownerTeam     = ownerTeam;
        this.flagUuid      = flagUuid;
        this.lastVaultTeam = ownerTeam;
    }

    // ── State transitions ─────────────────────────────────────────────────────

    public void setInVault(String vaultTeam) {
        this.state            = FlagState.IN_VAULT;
        this.holderUUID       = null;
        this.holderName       = null;
        this.droppedWorld     = null;
        this.droppedEntityUUID = null;
        this.lastVaultTeam    = vaultTeam;
    }

    public void setHeld(UUID holder, String holderName) {
        this.state      = FlagState.HELD;
        this.holderUUID = holder;
        this.holderName = holderName;
        this.droppedWorld = null;
        this.droppedEntityUUID = null;
    }

    public void setDropped(String world, double x, double y, double z, UUID entityUUID) {
        this.state             = FlagState.DROPPED;
        this.holderUUID        = null;
        this.holderName        = null;
        this.droppedWorld      = world;
        this.droppedX          = x;
        this.droppedY          = y;
        this.droppedZ          = z;
        this.droppedEntityUUID = entityUUID;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String    getOwnerTeam()        { return ownerTeam; }
    public UUID      getFlagUuid()         { return flagUuid; }
    public FlagState getState()            { return state; }

    public UUID   getHolderUUID()          { return holderUUID; }
    public String getHolderName()          { return holderName; }

    public String getDroppedWorld()        { return droppedWorld; }
    public double getDroppedX()            { return droppedX; }
    public double getDroppedY()            { return droppedY; }
    public double getDroppedZ()            { return droppedZ; }
    public UUID   getDroppedEntityUUID()   { return droppedEntityUUID; }

    public String getLastVaultTeam()       { return lastVaultTeam; }
    public void   setLastVaultTeam(String t){ this.lastVaultTeam = t; }

    public void setState(FlagState s)      { this.state = s; }
}
