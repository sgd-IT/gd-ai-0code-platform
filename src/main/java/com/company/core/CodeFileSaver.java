package com.company.core;

import cn.hutool.core.io.FileUtil;
import com.company.ai.model.HtmlCodeResult;
import com.company.ai.model.MultiFileCodeResult;
import com.company.ai.model.enums.CodenGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器
 */
public class CodeFileSaver {

    //文件保存到根目录
    private static final String FILE_PATH = System.getProperty("user.dir") + "/tmp/code_output";


    //保存HtmlCodeResult
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDirPath(CodenGenTypeEnum.HTML.getValue());
        saveSingleFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    //保存MultiFileCodeResult
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDirPath(CodenGenTypeEnum.MULTI_FILE.getValue());
        saveSingleFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        saveSingleFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        saveSingleFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }
    //构建唯一目录路径
    private static String buildUniqueDirPath(String type) {
        //File.sparator== \(斜杆)
        String baseDirPath = FILE_PATH + File.separator + type;
        File baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            //创建目录 mkdirs递归创建「多级嵌套文件夹」，哪怕父文件夹不存在，也会自动创建所有层级。
            baseDir.mkdirs();
        }
        return baseDirPath;
    }

    //保存单个文件
    private static void saveSingleFile(String baseDirPath, String fileName, String content) {
        String filePath = baseDirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

}
