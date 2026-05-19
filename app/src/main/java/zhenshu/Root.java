package zhenshu;

import android.util.Log;

import java.io.*;

/* loaded from: classes.dex */
public class Root {
    private static Process suProcess;

    public void setsuProcess() throws IOException {
        suProcess = Runtime.getRuntime().exec("su");
    }

    public Process getsuProcess() {
        return suProcess;
    }

    public static boolean isAppRooted() {
        boolean isRooted = false;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream.writeBytes("id\n");
            dataOutputStream.close();
            dataOutputStream.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            while (true) {
                String output = reader.readLine();
                if (output != null) {
                    if (output.toLowerCase().contains("uid=0")) {
                        isRooted = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            suProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRooted;
    }

    public String DUFile(String filePath) {
        try {

            suProcess = Runtime.getRuntime().exec("su");
            String command = "cat " + filePath;
            suProcess.getOutputStream().write((command + "\n").getBytes());
            suProcess.getOutputStream().write("exit\n".getBytes());
            suProcess.getOutputStream().flush();
            suProcess.waitFor();
            InputStream inputStream = suProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                } else {
                    reader.close();
                    inputStream.close();
                    String fileContent = stringBuilder.toString();

                    return fileContent;
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
            return "无";
        }
    }

    public int writeFile_modules() {
        writeFile_zj();
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream.writeBytes("mount -o rw,remount /system\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.close();
            dataOutputStream.flush();
            int exitCode = suProcess.waitFor();

            suProcess.destroy();
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream2 = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream2.writeBytes("mount -o rw,remount /\n");
            dataOutputStream2.writeBytes("exit\n");
            dataOutputStream2.close();
            dataOutputStream2.flush();
            int exitCode2 = suProcess.waitFor();

            suProcess.destroy();


            ShellUtils.execCmd(true,"mkdir -p /data/adb/modules/");
            ShellUtils.CommandResult commandResult1;
            String command212 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /data/adb/modules/298807fb.0";
            commandResult1 =ShellUtils.execCmd(true,command212);
            int exitCode3 = commandResult1.result;


            if (exitCode3 == 0) {


                ShellUtils.CommandResult commandResult;
                String command21 = "mkdir -p /data/adb/modules/sunny/certificates/";
                commandResult=ShellUtils.execCmd(true,command21);
                int exitCode41 = commandResult.result;


                String command2 = "mkdir -p /data/adb/modules/sunny/cert/";
                commandResult=ShellUtils.execCmd(true,command2);
                int exitCode4 = commandResult.result;


                String command3 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /data/adb/modules/sunny/cert";
                commandResult=ShellUtils.execCmd(true,command3);
                int exitCode5 =commandResult.result;

                if (exitCode5 != 0) {
                    return exitCode5;
                }

                String command5 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/sunny.sh /data/adb/modules/sunny/post-fs-data.sh";
                commandResult=ShellUtils.execCmd(true,command5);
                int exitCode6 = commandResult.result;


                writeFile_zj_xx();
                command5 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/module.prop /data/adb/modules/sunny";
                commandResult=ShellUtils.execCmd(true,command5);



                writeFile_zj_xz();
                String command6 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/uninstall.sh /data/adb/modules/sunny";
                commandResult=ShellUtils.execCmd(true,command6);


                String command51 = "chmod 777 /data/adb/modules/sunny/post-fs-data.sh";
                ShellUtils.execCmd(true,command51);
                command51 = "chmod 777 /data/adb/modules/sunny/uninstall.sh";
                ShellUtils.execCmd(true,command51);

                return exitCode6;
            }

            return 1;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("久久算法助手", String.valueOf(e));
            return 1;
        }
    }

    public int writeFile_lingshi() {
        writeFile_zj();
        try {
            ShellUtils.CommandResult commandResult;
            String command21 = "rm -rf /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            int exitCode41 = commandResult.result;


            command21 = "mkdir -p -m 700 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "mount -t tmpfs tmpfs /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "cp -f /system/etc/security/cacerts/* /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chmod 644 /data/local/tmp/sunnynet/*";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chown -R 0:0 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


           /* command21 = "set_context /system/etc/security/cacerts /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;
            Log.d("久久算法助手", "writeFile_lingshi>cacerts/*2>命令执行结束，退出码：" + exitCode41);*/


            command21 = "mount -o bind /data/local/tmp/sunnynet /system/etc/security/cacerts";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            return exitCode41;
        } catch (Exception e) {
            e.printStackTrace();

            return 1;
        }
    }

    public int writeFile_lingshi14() {
        writeFile_zj();
        try {
            ShellUtils.CommandResult commandResult;
            String command21 = "rm -rf /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            int exitCode41 = commandResult.result;


            command21 = "mkdir -p -m 700 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "mount -t tmpfs tmpfs /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "cp -f /apex/com.android.conscrypt/cacerts/* /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chown -R system:system /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chown root:shell /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chmod -R 644 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "chmod 755 /data/local/tmp/sunnynet";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "if [ \"$(getenforce)\" = \"Enforcing\" ]; then selinux_context=$(ls -Zd /apex/com.android.conscrypt/cacerts | awk '{print $1}'); if [ -n \"$selinux_context\" ] && [ \"$selinux_context\" != \"?\" ]; then chcon -R \"$selinux_context\" /data/local/tmp/sunnynet; else chcon -R u:object_r:system_file:s0 /data/local/tmp/sunnynet; fi; fi";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "mount -o bind /data/local/tmp/sunnynet /apex/com.android.conscrypt/cacerts";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "apex_dir=$(find /apex -type d -name 'com.android.conscrypt@*' | head -n 1); if [ -n \"$apex_dir\" ]; then mount -o bind /data/local/tmp/sunnynet \"$apex_dir/cacerts\"; fi";
            commandResult=ShellUtils.execCmd(true,command21);
            if (commandResult.result != 0) {
                return commandResult.result;
            }


            command21 = "test -f /apex/com.android.conscrypt/cacerts/298807fb.0";
            commandResult=ShellUtils.execCmd(true,command21);
            return commandResult.result;
        } catch (Exception e) {
            e.printStackTrace();

            return 1;
        }
    }

    public int writeFile_cacerts() {
        writeFile_zj();
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream.writeBytes("mount -o rw,remount /system\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.close();
            dataOutputStream.flush();
            int exitCode = suProcess.waitFor();

            suProcess.destroy();
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream2 = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream2.writeBytes("mount -o rw,remount /\n");
            dataOutputStream2.writeBytes("exit\n");
            dataOutputStream2.close();
            dataOutputStream2.flush();
            int exitCode2 = suProcess.waitFor();

            suProcess.destroy();
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream3 = new DataOutputStream(suProcess.getOutputStream());
            String command = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /system/etc/security/cacerts";
            dataOutputStream3.writeBytes(command + "\n");
            dataOutputStream3.writeBytes("exit\n");
            dataOutputStream3.close();
            dataOutputStream3.flush();
            int exitCode3 = suProcess.waitFor();

            suProcess.destroy();
            if (exitCode3 != 0) {
                return exitCode3;
            }
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream4 = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream4.writeBytes("chmod 644 /system/etc/security/cacerts/298807fb.0\n");
            dataOutputStream4.writeBytes("exit\n");
            dataOutputStream4.close();
            dataOutputStream4.flush();
            int exitCode4 = suProcess.waitFor();

            suProcess.destroy();
            return exitCode4;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("久久算法助手", String.valueOf(e));
            return 1;
        }
    }

    public int writeFile_cacerts14() {
        writeFile_zj();
        try {
            ShellUtils.CommandResult commandResult;
            String command21 = "mount -o rw,remount /";
            commandResult=ShellUtils.execCmd(true,command21);
            int exitCode41 = commandResult.result;


            command21 = "mount -o rw,remount /apex/com.android.conscrypt";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;


            command21 = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /apex/com.android.conscrypt/cacerts/298807fb.0";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;
            if (exitCode41 != 0) {
                return exitCode41;
            }

            command21 = "chmod 644 /apex/com.android.conscrypt/cacerts/298807fb.0";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;
            if (exitCode41 != 0) {
                return exitCode41;
            }


            command21 = "chown root:root /apex/com.android.conscrypt/cacerts/298807fb.0";
            commandResult=ShellUtils.execCmd(true,command21);
            exitCode41 = commandResult.result;
            if (exitCode41 != 0) {
                return exitCode41;
            }


            command21 = "apex_dir=$(find /apex -type d -name 'com.android.conscrypt@*' | head -n 1); if [ -n \"$apex_dir\" ]; then cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 \"$apex_dir/cacerts/298807fb.0\" && chmod 644 \"$apex_dir/cacerts/298807fb.0\" && chown root:root \"$apex_dir/cacerts/298807fb.0\"; fi";
            commandResult=ShellUtils.execCmd(true,command21);
            if (commandResult.result != 0) {
                return commandResult.result;
            }


            command21 = "test -f /apex/com.android.conscrypt/cacerts/298807fb.0";
            commandResult=ShellUtils.execCmd(true,command21);
            return commandResult.result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("久久算法助手", String.valueOf(e));
            return 1;
        }
    }

    public void writeFile(String data, String filePath, String filePath2) {
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            dataOutputStream.writeBytes("mount -o rw,remount /\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.close();
            dataOutputStream.flush();
            int exitCode = suProcess.waitFor();

            suProcess.destroy();

            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream2 = new DataOutputStream(suProcess.getOutputStream());
            String command = "cp -f /data/user/0/" + zswenjian.APP_filePath + "/298807fb.0 /system/etc/security/cacerts";
            dataOutputStream2.writeBytes(command + "\n");
            dataOutputStream2.writeBytes("exit\n");
            dataOutputStream2.close();
            dataOutputStream2.flush();
            int exitCode2 = suProcess.waitFor();

            suProcess.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
    }

    private static String execShell(String command) {
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                result = result + line;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void writeFile_zj() {
        String file = "/data/user/0/" + zswenjian.APP_filePath + "/298807fb.0";
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(zswenjian.zhengshu.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    public void writeFile_zj_xx() {
        String file = "/data/user/0/" + zswenjian.APP_filePath + "/module.prop";
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(zswenjian.zhengshu_xx.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void writeFile_zj_xz() {
        String file = "/data/user/0/" + zswenjian.APP_filePath + "/uninstall.sh";
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(zswenjian.xizai.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
