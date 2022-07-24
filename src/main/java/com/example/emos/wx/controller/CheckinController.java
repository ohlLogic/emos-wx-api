package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

@RequestMapping("/checkin")
@RestController
@Api("签到模块web接口")
@Slf4j
public class CheckinController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token)
    {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token){
        if(file == null){
            return R.error("没有上传文件");
        }

        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if(!fileName.endsWith(".jpg"))
        {
            return R.error("必须提交JPG格式图片");
        }
        else{
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get(path));
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("path", path);
                param.put("city", form.getCity());
                param.put("district", form.getDistrict());
                param.put("address", form.getAddress());
                param.put("country", form.getCountry());
                param.put("province", form.getProvince());
                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            }
            finally {
                // 删除path路径下面的照片
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token)
    {
        if(file == null){
            return R.error("没有上传文件");
        }

        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if(!fileName.endsWith(".jpg"))
        {
            return R.error("必须提交JPG格式图片");
        }
        else{
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get(path));

                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            }
            finally {
                // 删除path路径下面的照片
                FileUtil.del(path);
            }
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当前签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token)
    {
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        // 判断日期是否在用户入职之前
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if(startDate.isBefore(hiredate))
        {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }
}
