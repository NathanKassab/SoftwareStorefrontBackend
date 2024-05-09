package me.bannock.capstone.backend.loader.prot.service;

import java.util.Objects;

public class LoaderProtJobDto {

    /**
     * @param id The id of the job
     * @param owner The uid of the owner of this loader protection job
     * @param state The current state of the job
     * @param friendlyName The friendly <strong>unique</strong>name of the jar
     */
    public LoaderProtJobDto(String id, long owner, String state, String friendlyName){
        this.id = id;
        this.owner = owner;
        this.state = state;
        this.friendlyName = friendlyName;
    }

    private final String id;
    private final long owner;
    private final String state;
    private final String friendlyName;

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String toString() {
        return "LoaderProtJobDto{" +
                "id='" + id + '\'' +
                ", owner=" + owner +
                ", state='" + state + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LoaderProtJobDto that = (LoaderProtJobDto) object;
        return owner == that.owner && Objects.equals(id, that.id) && Objects.equals(state, that.state) && Objects.equals(friendlyName, that.friendlyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, state, friendlyName);
    }

}
