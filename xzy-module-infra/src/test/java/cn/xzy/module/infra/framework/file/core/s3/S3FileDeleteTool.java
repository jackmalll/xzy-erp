package cn.xzy.module.infra.framework.file.core.s3;

import cn.xzy.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.xzy.module.infra.framework.file.core.client.s3.S3FileClientConfig;

import java.util.Scanner;

/**
 * S3 文件删除工具
 * 用于删除误上传到云存储的文件
 *
 * 使用说明：
 * 1. 请先在七牛云控制台（https://portal.qiniu.com/）获取正确的 AccessKey 和 SecretKey
 * 2. 修改下面的配置信息（endpoint、bucket、accessKey、accessSecret）
 * 3. 运行 main 方法，按提示操作
 *
 * @author 芋道源码
 */
public class S3FileDeleteTool {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== S3 文件删除工具 ===");
        System.out.println();
        System.out.println("⚠️  重要提示：");
        System.out.println("1. 请确保您有正确的 AccessKey 和 SecretKey");
        System.out.println("2. 可以在七牛云控制台 > 个人中心 > 密钥管理 中查看");
        System.out.println("3. 删除操作不可逆，请谨慎操作");
        System.out.println();
        
        // 询问是否使用手动输入配置
        System.out.print("是否使用手动输入配置? (y/n，默认 n): ");
        String useManual = scanner.nextLine().trim().toLowerCase();
        
        S3FileClientConfig config;
        if ("y".equals(useManual) || "yes".equals(useManual)) {
            config = inputConfig(scanner);
        } else {
            config = getDefaultConfig();
        }
        
        if (config == null) {
            System.out.println("配置失败，退出程序");
            scanner.close();
            return;
        }

        // 创建 S3 客户端
        S3FileClient client = new S3FileClient(0L, config);
        client.init();

        System.out.println("配置信息：");
        System.out.println("  Endpoint: " + config.getEndpoint());
        System.out.println("  Bucket: " + config.getBucket());
        System.out.println();

        while (true) {
            System.out.println("请选择操作：");
            System.out.println("1. 删除单个文件");
            System.out.println("2. 批量删除文件");
            System.out.println("3. 列出文件（需要额外实现）");
            System.out.println("0. 退出");
            System.out.print("请输入选项: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        deleteSingleFile(client, scanner);
                        break;
                    case "2":
                        deleteBatchFiles(client, scanner);
                        break;
                    case "3":
                        System.out.println("列出文件功能需要额外实现，请使用云存储控制台查看文件列表");
                        break;
                    case "0":
                        System.out.println("退出程序");
                        scanner.close();
                        return;
                    default:
                        System.out.println("无效的选项，请重新输入");
                }
            } catch (Exception e) {
                System.err.println("操作失败: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        }
    }

    /**
     * 删除单个文件
     */
    private static void deleteSingleFile(S3FileClient client, Scanner scanner) throws Exception {
        System.out.print("请输入要删除的文件路径（例如：20240414/test.jpg）: ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("文件路径不能为空");
            return;
        }

        System.out.print("确认删除文件 [" + filePath + "] ? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if ("y".equals(confirm) || "yes".equals(confirm)) {
            client.delete(filePath);
            System.out.println("✓ 文件删除成功: " + filePath);
        } else {
            System.out.println("取消删除操作");
        }
    }

    /**
     * 批量删除文件
     */
    private static void deleteBatchFiles(S3FileClient client, Scanner scanner) throws Exception {
        System.out.println("请输入要删除的文件路径列表（每行一个路径，输入空行结束）:");

        StringBuilder paths = new StringBuilder();
        int count = 0;

        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                break;
            }
            paths.append(line).append("\n");
            count++;
        }

        if (count == 0) {
            System.out.println("没有输入任何文件路径");
            return;
        }

        System.out.println("共输入 " + count + " 个文件路径");
        System.out.print("确认删除这些文件? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if ("y".equals(confirm) || "yes".equals(confirm)) {
            String[] fileList = paths.toString().split("\n");
            int successCount = 0;
            int failCount = 0;

            for (String filePath : fileList) {
                try {
                    client.delete(filePath.trim());
                    System.out.println("✓ 删除成功: " + filePath);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("✗ 删除失败: " + filePath + " - " + e.getMessage());
                    failCount++;
                }
            }

            System.out.println();
            System.out.println("删除完成！成功: " + successCount + ", 失败: " + failCount);
        } else {
            System.out.println("取消删除操作");
        }
    }

    /**
     * 获取默认配置
     * 注意：请修改这里的 accessKey 和 accessSecret 为您自己的密钥
     */
    private static S3FileClientConfig getDefaultConfig() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.cn-south-1.qiniucs.com");
        config.setBucket("ruoyi-vue-pro");
        // ⚠️ 请修改为您自己的密钥！下面的密钥可能已失效
        config.setAccessKey("请在这里填写您的AccessKey");
        config.setAccessSecret("请在这里填写您的SecretKey");
        config.setEnablePathStyleAccess(false);
        config.setEnablePublicAccess(true);
        config.setRegion("cn-south-1");
        return config;
    }

    /**
     * 手动输入配置
     */
    private static S3FileClientConfig inputConfig(Scanner scanner) {
        S3FileClientConfig config = new S3FileClientConfig();
        
        System.out.println("请输入 S3 配置信息：");
        
        System.out.print("Endpoint (例如: s3.cn-south-1.qiniucs.com): ");
        config.setEndpoint(scanner.nextLine().trim());
        
        System.out.print("Bucket (例如: ruoyi-vue-pro): ");
        config.setBucket(scanner.nextLine().trim());
        
        System.out.print("AccessKey: ");
        config.setAccessKey(scanner.nextLine().trim());
        
        System.out.print("SecretKey: ");
        config.setAccessSecret(scanner.nextLine().trim());
        
        System.out.print("Region (例如: cn-south-1，可选): ");
        String region = scanner.nextLine().trim();
        if (!region.isEmpty()) {
            config.setRegion(region);
        }
        
        config.setEnablePathStyleAccess(false);
        config.setEnablePublicAccess(true);
        
        System.out.println();
        return config;
    }

    /**
     * 快速删除指定文件（用于直接在代码中调用）
     * 注意：使用前请先修改 accessKey 和 accessSecret
     */
    public static void quickDelete(String... filePaths) {
        // 使用默认配置
        S3FileClientConfig config = getDefaultConfig();

        // 创建 S3 客户端
        S3FileClient client = new S3FileClient(0L, config);
        client.init();

        System.out.println("开始删除文件...");
        for (String filePath : filePaths) {
            try {
                client.delete(filePath);
                System.out.println("✓ 删除成功: " + filePath);
            } catch (Exception e) {
                System.err.println("✗ 删除失败: " + filePath + " - " + e.getMessage());
            }
        }
        System.out.println("删除操作完成");
    }
}
