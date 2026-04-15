package cn.xzy.module.infra.framework.file.core.s3;

import cn.xzy.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.xzy.module.infra.framework.file.core.client.s3.S3FileClientConfig;

import java.util.Scanner;

/**
 * S3 文件验证工具
 * 用于验证文件是否真的存在于云存储中
 *
 * @author 芋道源码
 */
public class S3FileVerifyTool {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== S3 文件验证工具 ===");
        System.out.println();

        // 手动输入配置
        S3FileClientConfig config = inputConfig(scanner);

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
            System.out.println("1. 检查文件是否存在");
            System.out.println("2. 尝试删除文件");
            System.out.println("0. 退出");
            System.out.print("请输入选项: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        checkFileExists(client, scanner);
                        break;
                    case "2":
                        tryDeleteFile(client, scanner);
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
     * 检查文件是否存在
     */
    private static void checkFileExists(S3FileClient client, Scanner scanner) {
        System.out.print("请输入要检查的文件路径: ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("文件路径不能为空");
            return;
        }

        try {
            // 尝试获取文件内容
            byte[] content = client.getContent(filePath);
            System.out.println("✓ 文件存在！");
            System.out.println("  文件大小: " + content.length + " 字节");
            System.out.println("  文件路径: " + filePath);
        } catch (Exception e) {
            System.out.println("✗ 文件不存在或无法访问");
            System.out.println("  错误信息: " + e.getMessage());
        }
    }

    /**
     * 尝试删除文件
     */
    private static void tryDeleteFile(S3FileClient client, Scanner scanner) throws Exception {
        System.out.print("请输入要删除的文件路径: ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("文件路径不能为空");
            return;
        }

        // 先检查文件是否存在
        System.out.println("正在检查文件是否存在...");
        try {
            byte[] content = client.getContent(filePath);
            System.out.println("✓ 文件存在，大小: " + content.length + " 字节");
        } catch (Exception e) {
            System.out.println("✗ 文件不存在: " + e.getMessage());
            return;
        }

        // 确认删除
        System.out.print("确认删除文件 [" + filePath + "] ? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("取消删除操作");
            return;
        }

        // 执行删除
        System.out.println("正在删除文件...");
        try {
            client.delete(filePath);
            System.out.println("✓ 删除请求已发送");
        } catch (Exception e) {
            System.err.println("✗ 删除失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 验证删除结果
        System.out.println("正在验证删除结果...");
        Thread.sleep(2000); // 等待 2 秒
        try {
            client.getContent(filePath);
            System.out.println("⚠️  警告：文件仍然存在！删除可能失败了");
        } catch (Exception e) {
            System.out.println("✓ 验证成功：文件已被删除");
        }
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
}
