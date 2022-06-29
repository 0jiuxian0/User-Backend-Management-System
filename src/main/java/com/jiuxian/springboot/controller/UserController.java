package com.jiuxian.springboot.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiuxian.springboot.common.Constants;
import com.jiuxian.springboot.common.Result;
import com.jiuxian.springboot.controller.dto.UserDTO;
import com.jiuxian.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import com.jiuxian.springboot.service.IUserService;
import com.jiuxian.springboot.entity.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jiuxian
 * @since 2022-05-16
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        UserDTO dto = userService.login(userDTO);
        return Result.success(dto);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        return Result.success(userService.register(userDTO));
    }


    @PostMapping
    public Result save(@RequestBody User user) {
        return Result.success(userService.saveOrUpdate(user));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findOne(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username" ,username);
        return Result.success(userService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String username,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "") String address) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!"".equals(username)){
            queryWrapper.like("username",username);
        }
        if (!"".equals(email)){
            queryWrapper.like("email",email);
        }
        if (!"".equals(address)){
            queryWrapper.like("address",address);
        }
        queryWrapper.orderByDesc("id");//根据id倒序
        //获取当前用户信息
        User currentUser = TokenUtils.getCurrentUser();
        System.out.println("获取当前用户信息==========="+currentUser.getNickname());
        return Result.success(userService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    /**
     * 导出接口
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception{
        //从数据库查询出所有的数据
        List<User> list = userService.list();
        //通过工具类创建writer写出到磁盘路径
        //ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath+"/用户信息.xlsx");
        //从内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        //writer.addHeaderAlias("id","id");
        //writer.addHeaderAlias("username","用户名");
        //writer.addHeaderAlias("password","密码");
        //writer.addHeaderAlias("nickname","昵称");
        //writer.addHeaderAlias("email","邮箱");
        //writer.addHeaderAlias("phone","电话");
        //writer.addHeaderAlias("address","地址");
        //writer.addHeaderAlias("createTime","创建时间");
        //writer.addHeaderAlias("avatarUrl","头像");
        //一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list,true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true);
        out.close();
        writer.close();
    }

    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception{
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        //表头要求英文
        List<User> list = reader.readAll(User.class);
        userService.saveBatch(list);
        return Result.success(true);
    }
}

