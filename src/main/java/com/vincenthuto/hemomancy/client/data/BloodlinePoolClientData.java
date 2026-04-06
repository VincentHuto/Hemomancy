package com.vincenthuto.hemomancy.client.data;

/**
 * Client-side cache for the shared bloodline pool status.
 * Updated by PacketSyncBloodlinePool from the server.
 */
public class BloodlinePoolClientData {

	private static float poolVolume = 0f;
	private static float poolMax = 0f;
	private static int memberCount = 0;

	public static float getPoolVolume() {
		return poolVolume;
	}

	public static float getPoolMax() {
		return poolMax;
	}

	public static int getMemberCount() {
		return memberCount;
	}

	public static void set(float volume, float max, int members) {
		poolVolume = volume;
		poolMax = max;
		memberCount = members;
	}

	public static void clear() {
		poolVolume = 0f;
		poolMax = 0f;
		memberCount = 0;
	}
}
