package com.vincenthuto.hemomancy.common.capability.player.scar;

import java.util.concurrent.Callable;

public class ScarCap {


	public static class IScarFactory implements Callable<IScar> {

		@Override
		public IScar call() {
			return () -> ScarType.OVERRIDE;
		}
	}
}
