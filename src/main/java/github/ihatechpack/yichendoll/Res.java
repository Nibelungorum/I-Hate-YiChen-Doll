package github.ihatechpack.yichendoll;

import net.minecraft.resources.ResourceLocation;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2025/9/21 11:59
 */
public class Res {
    // 批量注册玩偶
    // public static final String[] howxu = {"howxu"};
    // public static final String[] hualeibao = {"yuanxi_19","dchen233"};
    // public static final String[] specials = {"aluo","qingjiao","budai","yichen_winter","m_xm_"};
    // public static final String[] yichens = {
    //         "yichen_angel",
    //         "yichen_chachuqu",
    //         "yichen_chachuqu2",
    //         "yichen_dan",
    //         "yichen_mm",
    //         "yichen_nvpu",
    //         "yichen_nvwu",
    //         "yichen_puxin",
    //         "yichen_xixi",
    //         "yichen_zhale"
    // };
    // public static final String[] tech_guys = {
    //         "big_yusen",
    //         "bury_love",
    //         "gotopark",
    //         "ohana_1234",
    //         "riji_",
    //         "suifengyao"
    // };
    public static ResourceLocation rl(String pPath){
        return ResourceLocation.tryBuild(IHateYiChenDoll.MOD_ID,pPath);
    }
}
