package main.provider;

import main.constant.EnumCommonConfig;

public class SystemDefaultCommonConfigProvider {

	public int getDefaultVal() {
		// 実際はDBなどから取得してくる
		return EnumCommonConfig.UNUSE.getVal();
	}
}
