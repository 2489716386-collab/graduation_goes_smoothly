package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "公共-文件上传")
public class UploadController {

    // 定义图片在服务器本地的保存路径 (可以根据你的电脑实际情况修改，比如 D:/pet_health/uploads/)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/image")
    @Operation(summary = "上传图片")
    public Result uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 确保目录存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 生成唯一文件名，防止覆盖
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件到本地
            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            // 返回可以访问该图片的URL (假设你的后端运行在 8080 端口)
            // 注意：这里需要配合后面的 WebMvcConfig 静态资源映射才能访问
            String imageUrl = "http://localhost:8080/uploads/" + newFileName;
            return Result.success(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }
}
