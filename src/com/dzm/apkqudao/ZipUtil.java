package com.dzm.apkqudao;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.tools.ant.filters.StringInputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
/**
 * Created by 83642 on 2017/8/9.
 */
public class ZipUtil {
    private static final int BUFFER = 1024;
    private static final String BASE_DIR = "";
    /**符号"/"用来作为目录标识判断符*/
    private static final String PATH = "/";

    /**签名目录*/
    public static final String SIGN_PATH_NAME = "META-INF";
    /**修改文件目录*/
    public static final String UPDATE_PATH_NAME = "\\assets\\qudao.txt";
    /**解压源文件目录*/
    public static final String SOURCE_PATH_NAME = "\\source\\";
    /**打包目录*/
    public static final String TARGET_PATH_NAME = "\\target\\";
    /**签名目录*/
    private static final String RESULT_PATH_NAME = "\\result\\";
    /**JDK BIN 目录*/
    private static final String JDK_BIN_PATH = "C:\\Program Files\\Java\\jdk1.8.0_91\\bin";
    /**密钥 目录*/
    private static final String SECRET_KEY_PATH = "D:\\androidstudio\\worspaceYiZun\\渠道包\\打包测试";
    /**密钥 名称*/
    private static final String SECRET_KEY_NAME = "ej_v3.jks";

    /**
     * 解压缩zip文件
     * @param fileName 要解压的文件名 包含路径 如："c:\\test.zip"
     * @param filePath 解压后存放文件的路径 如："c:\\temp"
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void unZip(String fileName, String filePath) throws Exception{
        ZipFile zipFile = new ZipFile(fileName);
        Enumeration emu = zipFile.getEntries();

        while(emu.hasMoreElements()){
            ZipArchiveEntry entry = (ZipArchiveEntry) emu.nextElement();
            if (entry.isDirectory()){
                new File(filePath+entry.getName()).mkdirs();
                continue;
            }
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

            File file = new File(filePath + entry.getName());
            File parent = file.getParentFile();
            if(parent != null && (!parent.exists())){
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);

            byte [] buf = new byte[BUFFER];
            int len = 0;
            while((len=bis.read(buf,0,BUFFER))!=-1){
                fos.write(buf,0,len);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
        zipFile.close();
    }

    /**
     * 压缩文件
     *
     * @param srcFile
     * @param destPath
     * @throws Exception
     */
    public static void compress(String srcFile, String destPath) throws Exception {
        compress(new File(srcFile), new File(destPath));
    }

    /**
     * 压缩
     *
     * @param srcFile
     *            源路径
     *            目标路径
     * @throws Exception
     */
    public static void compress(File srcFile, File destFile) throws Exception {
        // 对输出文件做CRC32校验
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                destFile), new CRC32());

        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(srcFile, zos, BASE_DIR);

        zos.flush();
        zos.close();
    }

    /**
     * 压缩
     *
     * @param srcFile
     *            源路径
     * @param zos
     *            ZipOutputStream
     * @param basePath
     *            压缩包内相对路径
     * @throws Exception
     */
    private static void compress(File srcFile, ZipOutputStream zos,
                                 String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    /**
     * 压缩目录
     *
     * @param dir
     * @param zos
     * @param basePath
     * @throws Exception
     */
    private static void compressDir(File dir, ZipOutputStream zos,
                                    String basePath) throws Exception {
        File[] files = dir.listFiles();
        // 构建空目录
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);

            zos.putNextEntry(entry);
            zos.closeEntry();
        }

        String dirName = "";
        String path = "";
        for (File file : files) {
            //当父文件包名为空时，则不把包名添加至路径中（主要是解决压缩时会把父目录文件也打包进去）
            if(basePath!=null && !"".equals(basePath)){
                dirName=dir.getName();
            }
            path = basePath + dirName + PATH;
            // 递归压缩
            compress(file, zos, path);
        }
    }

    /**
     * 文件压缩
     *
     * @param file
     *            待压缩文件
     * @param zos
     *            ZipOutputStream
     * @param dir
     *            压缩文件中的当前路径
     * @throws Exception
     */
    private static void compressFile(File file, ZipOutputStream zos, String dir)
            throws Exception {
        /**
         * 压缩包内文件名定义
         *
         * <pre>
         * 如果有多级目录，那么这里就需要给出包含目录的文件名
         * 如果用WinRAR打开压缩包，中文名将显示为乱码
         * </pre>
         */
        if("/".equals(dir))dir="";
        else if(dir.startsWith("/"))dir=dir.substring(1,dir.length());

        ZipEntry entry = new ZipEntry(dir + file.getName());
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = bis.read(data, 0, BUFFER)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();

        zos.closeEntry();
    }

    public static void main(String[] args)throws Exception{
//        StringBuffer buffer = new StringBuffer();
//        BufferedReader br =null;
//        OutputStreamWriter osw =null;
//        String srcPath = "D:\\\\androidstudio\\\\worspaceYiZun\\\\渠道包\\\\打包测试\\\\app-debug.apk";
//        String content= "channel_id=LD20120926";
//
//        File srcFile = new File(srcPath);
//        String parentPath = srcFile.getParent();    //源文件目录
//        String fileName = srcFile.getName();        //源文件名称
//        String prefixName = fileName.substring(0, fileName.lastIndexOf("."));
//        //解压源文件保存路径
//        String sourcePath = buffer.append(parentPath).append(SOURCE_PATH_NAME).
//                append(prefixName).append("\\").toString();
//        System.out.println("sourcePath:"+sourcePath);
//        //------解压
//        unZip(srcPath, sourcePath);
//
//        //------删除解压后的签名文件
//        String signPathName = sourcePath+SIGN_PATH_NAME;
//        File signFile = new File(signPathName);
//        if(signFile.exists()){
//            File sonFiles[] = signFile.listFiles();
//            if(sonFiles!=null && sonFiles.length>0){
//                //循环删除签名目录下的文件
//                for(File f : sonFiles){
//                    f.delete();
//                }
//            }
//            signFile.delete();
//        }
//
//        //------修改内容
//        buffer.setLength(0);
//        String path = buffer.append(parentPath).append(SOURCE_PATH_NAME)
//                .append(prefixName).append(UPDATE_PATH_NAME).toString();
//        System.out.println(path);
//        br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
//        while((br.readLine())!=null)
//        {
//            osw = new OutputStreamWriter(new FileOutputStream(path));
//            osw.write(content,0,content.length());
//            osw.flush();
//        }
//
//        //------打包
//        String targetPath = parentPath+TARGET_PATH_NAME;
//        //判断创建文件夹
//        File targetFile = new File(targetPath);
//        if(!targetFile.exists()){
//            targetFile.mkdir();
//        }
//        compress(parentPath+SOURCE_PATH_NAME+prefixName,targetPath+fileName);


//        String line = null;
//        StringBuilder sb = new StringBuilder();
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process process = runtime.exec("adb devices");
//            BufferedReader  bufferedReader = new BufferedReader
//                    (new InputStreamReader(process.getInputStream()));
//
//
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line + "\n");
//
//            }
//            System.out.println(sb);
//        } catch (IOException e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//        }

        File file = new File("D:\\androidstudio\\worspaceYiZun\\渠道包\\打包测试\\a.txt");
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos,1024);
        InputStream inputStream = new StringInputStream("aaaaaaaa");
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(bytes))!=-1){
            bos.write(bytes,0,len);
        }
        bos.close();
        fos.close();
        inputStream.close();
        System.out.println(file.getAbsolutePath());
    }
}
