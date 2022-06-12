package test.main;

import static main.constant.EnumCommonConfig.DEPEND_ON_EMPLOYEE;
import static main.constant.EnumCommonConfig.UNDEFINED;
import static main.constant.EnumCommonConfig.UNUSE;
import static main.constant.EnumCommonConfig.USE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import main.FunctionAuthManager;
import main.constant.EnumCommonConfig;
import main.constant.UserConfigConst;
import main.provider.SystemDefaultCommonConfigProvider;
import main.vo.UseServiceConfigVo;

class FunctionAuthManagerTest extends FunctionAuthManager {
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("共通設定に関するテスト")
	class CanManageEachConfigByEmployeeTest {

		// システムデフォルト設定を返してくれるクラス
		private SystemDefaultCommonConfigProvider mockedDefaultConfigProvider;

		@BeforeAll
		public void setup() {
			mockedDefaultConfigProvider = spy(new SystemDefaultCommonConfigProvider());
		}

		@Test
		@DisplayName("管理者が設定した共通設定が2の場合に、メソッド：canManageEachConfigByEmployeeがtrueになるか")
		void testWithCommonConfigDependOnEmployee() {
			assertTrue(canManageEachConfigByEmployee(DEPEND_ON_EMPLOYEE, mockedDefaultConfigProvider));
		}

		@ParameterizedTest
		@EnumSource(value = EnumCommonConfig.class, names = { "UNUSE", "USE" })
		@DisplayName("管理者が設定した共通設定が0または1の場合に、メソッド：canManageEachConfigByEmployeeがfalseになるか")
		void testWithCommonConfigUseOrUnuse(EnumCommonConfig commonConfig) {
			assertFalse(canManageEachConfigByEmployee(commonConfig, mockedDefaultConfigProvider));
		}

		@ParameterizedTest
		@DisplayName("管理者が共通設定を未設定の場合に、メソッド：canManageEachConfigByEmployeeがシステムデフォルト共通設定に応じたboolean値を返すか")
		@CsvSource({ "UNDEFINED, UNUSE", "UNDEFINED, DEPEND_ON_EMPLOYEE", "UNDEFINED, USE", // 通常ありえないはずだが念のためテスト
				"UNDEFINED, UNDEFINED" // 通常ありえないはずだが念のためテスト
		})
		void testWithCommonConfigUndefined(EnumCommonConfig commonConfig, EnumCommonConfig systemDefaultCommonConfig) {
			assertTrue(commonConfig == UNDEFINED);

			when(mockedDefaultConfigProvider.getDefaultVal()).thenReturn(systemDefaultCommonConfig.getVal());

			switch (systemDefaultCommonConfig) {
			case UNUSE:
			case USE:
				assertFalse(canManageEachConfigByEmployee(commonConfig, mockedDefaultConfigProvider));
				break;
			case UNDEFINED:
				// システムデフォルトが万一存在しなかった場合でもExceptionが発生しないように
				assertDoesNotThrow(() -> canManageEachConfigByEmployee(commonConfig, mockedDefaultConfigProvider));
				break;
			default:
				break;
			}
		}
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("サービスアクセス可否を判断するためのメソッド：がcanUseSerciceが正しく動作するか")
	class CanUseServiceTest {

		@ParameterizedTest(name = "{index} ==> [期待結果：{1}] {2}")
		@MethodSource({ "authTypeUndefinedPatternsProvider", "authTypeUseOrUnusePatternsProvider",
				"authTypeDependOnBossPatternsProvider" })
		@DisplayName("canUseSerciceが共通設定とユーザー設定双方の利用設定を読んで表示制御できているか")
		void testWithComAuthTypeUndefined(UseServiceConfigVo vo, boolean expected, String description) {
			assertThat(canUseService(vo), is(expected));
		}

		/**
		 * 共通設定で利用設定が未設定の場合、システムデフォルト値で利用判定をできるか
		 *
		 * @return ・共通設定での設定値がされていない、かつ共通設定のシステムデフォルト値が1の場合<br>
		 *         ・共通設定での設定値がされていない、かつ共通設定のシステムデフォルト値が2の場合
		 */
		Stream<Arguments> authTypeUndefinedPatternsProvider() {
			return Stream.of(
					arguments(
							new UseServiceConfigVo.Builder().commonConfig(UNDEFINED).systemDefaultVal(UNUSE.getVal())
									.existsUserConfig(false).userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_USE).build(),
							false, "共通設定での設定値がされていない、かつ共通設定のシステムデフォルト値が1の場合"),
					arguments(
							new UseServiceConfigVo.Builder().commonConfig(UNDEFINED).systemDefaultVal(USE.getVal())
									.existsUserConfig(false).userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_USE).build(),
							true, "共通設定での設定値がされていない、かつ共通設定のシステムデフォルト値が2の場合"));
		}

		/**
		 * @return ・共通設定での設定値が1の場合<br>
		 *         ・共通設定での設定値が2の場合
		 */
		Stream<Arguments> authTypeUseOrUnusePatternsProvider() {
			return Stream.of(
					arguments(
							new UseServiceConfigVo.Builder().commonConfig(UNUSE).systemDefaultVal(USE.getVal())
									.existsUserConfig(false).userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_USE).build(),
							false, "共通設定での設定値が1の場合"),
					arguments(new UseServiceConfigVo.Builder().commonConfig(USE).systemDefaultVal(UNUSE.getVal())
							.existsUserConfig(false).userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_UNUSE).build(), true,
							"共通設定での設定値が2の場合"));
		}

		/**
		 * @return ・共通設定での設定値が3の場合、かつユーザー設定が0の場合<br>
		 *         ・共通設定での設定値が3の場合、かつユーザー設定が1の場合<br>
		 *         ・共通設定での設定値が3の場合、かつユーザー設定が存在しない場合
		 */
		Stream<Arguments> authTypeDependOnBossPatternsProvider() {
			return Stream.of(arguments(
					new UseServiceConfigVo.Builder().commonConfig(DEPEND_ON_EMPLOYEE).systemDefaultVal(USE.getVal())
							.existsUserConfig(true).userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_UNUSE).build(),
					false, "共通設定での設定値が3の場合、かつユーザー設定が0の場合"),
					arguments(
							new UseServiceConfigVo.Builder().commonConfig(DEPEND_ON_EMPLOYEE)
									.systemDefaultVal(USE.getVal()).existsUserConfig(true)
									.userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_USE).build(),
							true, "共通設定での設定値が3の場合、かつユーザー設定が1の場合"),
					arguments(
							new UseServiceConfigVo.Builder().commonConfig(DEPEND_ON_EMPLOYEE)
									.systemDefaultVal(USE.getVal()).existsUserConfig(false)
									.userConfigVal(UserConfigConst.EMPLOYEE_CONFIG_USE).build(),
							false, "共通設定での設定値が3の場合、かつユーザー設定が存在しない場合"));
		}

	}

}
