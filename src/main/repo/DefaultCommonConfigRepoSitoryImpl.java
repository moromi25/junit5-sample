package main.repo;

import main.constant.EnumCommonConfig;

public class DefaultCommonConfigRepoSitoryImpl implements DefaultCommonConfigRepository {

	@Override
	public int getDefaultVal() {
		// TODO DBから取ってくる
		return EnumCommonConfig.USE.getVal();
	}

}
