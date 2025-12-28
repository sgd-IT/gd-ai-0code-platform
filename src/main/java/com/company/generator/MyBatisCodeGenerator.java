package com.company.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;

import java.util.Map;


/**
 * Mybatis Flex 代码生成器
 */
public class MyBatisCodeGenerator {

    // 指定要生成的表
    private static final String[] TABLE_NAMES = {"user"};

    public static void main(String[] args) {
        // 使用hutool工具的yaml工具类加载配置文件
        Dict dict = YamlUtil.loadByPath("application-local.yml");
        Map<String, Object> datasource = dict.getByPath("spring.datasource");
        String url = String.valueOf(datasource.get("url"));
        String username = String.valueOf(datasource.get("username"));
        String password = String.valueOf(datasource.get("password"));

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }


    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包,先生成到一个临时目录下，生成完成后再移动到指定目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.company.generresult");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                // 逻辑删除字段
                .setLogicDeleteColumn("isDelete")
                .setGenerateTable(TABLE_NAMES);

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        //设置生成mapper xml
        globalConfig.enableMapperXml();

        //设置生成 service
        globalConfig.enableService();
        //设置生成 serviceImpl
        globalConfig.enableServiceImpl();

        //设置生成 controller
        globalConfig.enableController();

        //设置生成注释
        globalConfig.getJavadocConfig()
                .setAuthor("gd")
                .setSince("");


        return globalConfig;
    }

}
