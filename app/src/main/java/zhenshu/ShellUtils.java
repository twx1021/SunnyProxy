package zhenshu;

import android.util.Log;
import androidx.annotation.NonNull;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/8/7
 *     desc  : Shell相关工具类
 * </pre>
 */
public class ShellUtils {


    private ShellUtils() {
        throw new UnsupportedOperationException("u can't fuck me...");
    }

    public static final String COMMAND_SU = "su -mm";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    /**
     * 判断设备是否root
     *
     * @return {@code true}: root<br>{@code false}: 没root
     */
    public static boolean isRoot() {
        return execCmd("echo root", true, false).result == 0;
    }

    /**
     * 判断su是否存在(无需ROOT授权)
     *
     * @return
     */
    public static boolean isSUExist() {
        return execCmd("which su", false, false).result == 0 || findCmdPath("su") != null;
    }

    public static boolean isCmdExist(String cmd) {
        return execCmd("which " + cmd, false, false).result == 0 || findCmdPath(cmd) != null;
    }

    private static String findCmdPath(String cmd) {
        for (String path : Objects.requireNonNull(System.getenv("PATH")).split(":")) {
            File su = new File(path, cmd);
            if (su.exists() || su.canExecute()) {
//            if (su.canExecute()) {
                // We don't actually know whether the app has been granted root access.
                // Do NOT set the value as a confirmed state.
                return path;
            }
        }
        return null;

    }

    /**
     * 是否是在root下执行命令
     *
     * @param command 命令
     * @param isRoot  是否root
     * @return CommandResult
     */
    public static CommandResult execCmd(boolean isRoot, String... command) {
        CommandResult commandResult = execCmd(command, isRoot, true);
        if(commandResult.result == -1||commandResult.successMsg == null){
            Log.i("久久算法助手","ShellUtils->execCmd try no root ");
            //try no root cmd
            commandResult = execCmd(command, false, true);
        }
        return commandResult;
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands 多条命令链表
     * @param isRoot   是否root
     * @return CommandResult
     */
    public static CommandResult execCmd(List<String> commands, boolean isRoot) {
        return execCmd(commands == null ? null : commands.toArray(new String[]{}), isRoot, true);
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands 多条命令数组
     * @param isRoot   是否root
     * @return CommandResult
     */
    public static CommandResult execCmd(String[] commands, boolean isRoot) {
        return execCmd(commands, isRoot, true);
    }

    /**
     * 是否是在root下执行命令
     *
     * @param command         命令
     * @param isRoot          是否root
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    public static CommandResult execCmd(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands        命令链表
     * @param isRoot          是否root
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    public static CommandResult execCmd(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCmd(commands == null ? null : commands.toArray(new String[]{}), isRoot, isNeedResultMsg);
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean getRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            Log.e("久久算法助手","get root error 111111 " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Log.e("久久算法助手","get root error 222222" + e.getMessage());
            }
        }
        return true;
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands        命令数组
     * @param isRoot          是否root
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    public static CommandResult execCmd(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            Log.e("久久算法助手","execCmd error commands == null || commands.length == 0 " +(commands == null)+" "+(Objects.requireNonNull(commands).length == 0));
            return new CommandResult(result, null, null,null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                    successMsg.append(COMMAND_LINE_END);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                    successMsg.append(COMMAND_LINE_END);
                }
            } else {
                // InputStream和OutputStream的size是有限的,如果不及时处理掉就会把程序block住
                // 把流(InputStream/OutputStream)里的数据读取出来, 避免waitFor卡死
                while ((successResult.readLine()) != null) {
                }
                while ((errorResult.readLine()) != null) {
                }
            }
            result = process.waitFor();
        } catch (Throwable e) {
            Log.e("久久算法助手","ShellUtils cmd error "+e,e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString(),commands);
    }

    public static boolean execShell(String[] cmd) {
        Log.e("久久算法助手","execShell cmd -> " + Arrays.toString(cmd));
        Process process = null;
        DataOutputStream os = null;
        try {
            //process = Runtime.getRuntime().exec("su -mm"+"\n");
            process = Runtime.getRuntime().exec("sh "+"\n");
            os = new DataOutputStream(process.getOutputStream());
            for(String string:cmd) {
                os.writeBytes(string + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            int exitCode  = process.waitFor();

            BufferedReader brSuccess = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder successMsg = new StringBuilder();
            while ((line = brSuccess.readLine()) != null) {
                successMsg.append(line).append("\n");
            }

            // 错误输出
            BufferedReader brError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            while ((line = brError.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

            // 根据退出码判断成功或失败
            if (exitCode != 0) {
                Log.e("久久算法助手","execShell error " + Arrays.toString(cmd));
                Log.e("久久算法助手","Error Msg -> " + errorMsg);
                return false;
            } else {
                Log.i("久久算法助手","execShell success " + Arrays.toString(cmd));
                Log.i("久久算法助手","Success Msg -> " + successMsg);
                return true;
            }
        }
        catch (Throwable e) {
            Log.e("久久算法助手","execShell get root error  " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Log.e("久久算法助手","execShell get root error  " + e);
            }
        }
        return false;
    }
    public static boolean execShell(String cmd) {
        return execShell(new String[]{cmd});
    }

    /**
     * 返回的命令结果
     */
    public static class CommandResult {

        /**
         * 结果码
         **/
        public int result;
        /**
         * 成功的信息
         **/
        public String successMsg;
        /**
         * 错误信息
         **/
        public String errorMsg;

        /**
         * 原始cmd
         */
        public String[] cmd;
        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg,String[] commands) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
            this.cmd = commands;
//            if(!StringUtils.isEmpty(this.errorMsg)) {
//                Log.d("久久算法助手","CommandResult error -> " + this.toString());
//            }
        }

        @NonNull
        @Override
        public String toString() {
            return "CommandResult{" +
                    "result=" + result +
                    ", successMsg='" + successMsg + '\'' +
                    ", errorMsg='" + errorMsg + '\'' +
                    ", cmd='" + Arrays.toString(cmd) + '\'' +
                    '}';
        }
    }
}