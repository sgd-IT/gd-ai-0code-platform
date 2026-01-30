package com.company.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;


public abstract class CodeFileSaverTemplate<T> {
    //文件保存到根目录
    protected static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    //保存代码的流程模板
    public final File saveCode(T codeResult) {
        //校验输入
        validateInput(codeResult);
        //构建唯一目录路径
        String uniquePath = buildUniqueDirPath();
        //保存文件
        saveFiles(uniquePath, codeResult);
        //返回保存的目录
        return new File(uniquePath);
    }


    protected void validateInput(T codeResult) {
        if (codeResult == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, " 代码结果对象输入为空");
        }
    }

    //构建唯一目录路径
    protected final String buildUniqueDirPath() {
        //File.sparator== \(斜杆)
        String codeType = getCodeType().getValue();
        String uniquePath = StrUtil.format("{} {}", codeType, IdUtil.getSnowflakeNextIdStr());
        String baseDirPath = FILE_SAVE_ROOT_PATH + File.separator + uniquePath;
        File baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            //创建目录 mkdirs递归创建「多级嵌套文件夹」，哪怕父文件夹不存在，也会自动创建所有层级。
            baseDir.mkdirs();
        }
        return baseDirPath;
    }

    /**
     * 写入单个文件的工具方法
     * @param dirPath 目录路径
     * @param fileName 文件名
     * @param content 文件内容
     */
    protected final void writeToFile(String dirPath,String fileName, String content) {
        if(StrUtil.isNotBlank(content)){
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
        }
    }

    protected abstract void saveFiles(String uniquePath, T codeResult);

    protected abstract CodeGenTypeEnum getCodeType();

}

