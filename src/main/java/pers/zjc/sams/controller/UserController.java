package pers.zjc.sams.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pers.zjc.sams.model.Wrapper;
import pers.zjc.sams.po.Device;
import pers.zjc.sams.po.Student;
import pers.zjc.sams.po.Teacher;
import pers.zjc.sams.po.User;
import pers.zjc.sams.service.DeviceService;
import pers.zjc.sams.service.UserService;
import pers.zjc.sams.utils.Const;
import pers.zjc.sams.utils.Logger;
import pers.zjc.sams.utils.Result;
import pers.zjc.sams.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller
@ResponseBody
@RequestMapping(value = "api/mobile/users", method = RequestMethod.POST)
public class UserController extends BaseController{

    private static final String TAG = "UserController";

    Logger logger = Logger.getLogger(TAG);

    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;

    /**
     *
     * 查询所有用户
     */
    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Result getUsers() {
        List<User> userList = userService.getAllUsers();
        if (userList != null && userList.size() > 0) {
            Map map = new LinkedHashMap<>();
            map.put("users", userList);
            return Result.build(Const.HttpStatusCode.HttpStatus_200, "查询用户列表成功",userList);
        }
        return Result.fail_500();
    }

    /**
     *
     * 注册
     */
    @ResponseBody
    @RequestMapping(value = "register")
    public Result register(@RequestBody Wrapper param) {
        if (param == null) {
            return Result.fail_500("参数为空");
        }
        User user = param.getUser();
        Device device = param.getDevice();
        if (user == null || StringUtils.isEmpty(user.getAccount()) || StringUtils.isEmpty(user.getPassword())) {
            return Result.build(Const.HttpStatusCode.HttpStatus_401, "账号或密码不能为空", new Object());
        }
        if (user.getRole() == null) {
            return Result.build(Const.HttpStatusCode.HttpStatus_403, "用户角色不能为空");
        }
        try {
            //管理员
            if (user.getRole() == 0) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "您没有权限注册管理员账号");
            }
            if (userService.isExisted(user)) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "该账号已被注册");
            }
            switch (user.getRole()) {
                //学生
                case 1:
                    Student student = new Student();
                    Student maxIdStu = userService.getMaxIdStu();
                    if (maxIdStu == null) {
                        student.setStuId(Const.DEFAULT_ID.DEFAULT_STU_ID);
                        user.setId(Const.DEFAULT_ID.DEFAULT_STU_ID);
                    } else {
                        student.setStuId(maxIdStu.getStuId() + 1);
                        student.setsName(user.getAccount());
                        student.setClassId(1501);
                        user.setId(maxIdStu.getStuId() + 1);
                    }
                    if (userService.addStudent(student)) {
                        device.setStuId(student.getStuId());
                        if (StringUtils.isEmpty(device.getDeviceId())) {
                            return Result.fail_500("设备编号不能为空");
                        }
                        if (deviceService.isDeviceExisted(device.getDeviceId())) {
                            return Result.fail_500("此设备已经注册，若有疑问，请联系管理员");
                        }
                        if (deviceService.addStuDevice(device)) {
                            if (userService.updateUser(user)) {
                                return Result.ok("学生添加成功且设备绑定成功");
                            }
                        } else {
                            return Result.fail_500("设备绑定失败");
                        }
                    }
                    break;
                //教师
                case 2:
                    Teacher teacher = new Teacher();
                    Teacher maxIdTeacher = userService.getMaxIdTeacher();
                    if (maxIdTeacher == null || maxIdTeacher.getId() == null) {
                        teacher.setId(Const.DEFAULT_ID.DEFAULT_TEAC_ID);
                        user.setId(Const.DEFAULT_ID.DEFAULT_TEAC_ID);
                    } else {
                        teacher.setId(maxIdTeacher.getId() + 1);
                        user.setId(maxIdTeacher.getId() + 1);
                    }
                    if (userService.addTeacher(teacher)) {
                        if (userService.updateUser(user)) {
                            return Result.ok("教师添加成功");
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500();
        }
        return Result.fail_500("用户添加失败");
    }

    /**
     * 学生信息查询
     * */
    @ResponseBody
    @RequestMapping(value = "/students/info", method = RequestMethod.POST)
    public Result getStudent(@RequestBody Student student) {
        if (StringUtils.isEmpty(String.valueOf(student.getStuId()))) {
            return Result.build(Const.HttpStatusCode.HttpStatus_401, "学号不能为空");
        }
        Student stu = userService.getStudent(student);
        if (stu != null) {
            return Result.build(Const.HttpStatusCode.HttpStatus_200, "学生信息查询成功", stu);
        } else {
            return Result.build(Const.HttpStatusCode.HttpStatus_500, "学生信息查询失败", new Object());
        }
    }

    /**
     * 教师信息查询
     * */
    @ResponseBody
    @RequestMapping(value = "/teachers/info", method = RequestMethod.POST)
    public Result getStudent(@RequestBody Teacher teacher) {
        if (StringUtils.isEmpty(String.valueOf(teacher.getId()))) {
            return Result.build(Const.HttpStatusCode.HttpStatus_401, "教师编号不能为空");
        }
        Teacher teac = userService.getTeacher(teacher);
        if (teac != null) {
            return Result.build(Const.HttpStatusCode.HttpStatus_200, "教师信息查询成功", teac);
        } else {
            return Result.build(Const.HttpStatusCode.HttpStatus_500, "教师信息查询失败", new Object());
        }
    }

    /**
     * 学生信息修改
     * */
    @ResponseBody
    @RequestMapping(value = "/students/info/modify", method = RequestMethod.POST)
    public Result modifyStudent(@RequestBody Student student) {
        if (StringUtils.isEmpty(String.valueOf(student.getStuId()))) {
            return Result.build(Const.HttpStatusCode.HttpStatus_401, "学号不能为空");
        }
        if (userService.modifyStudent(student)) {
            Student stu = userService.getStudent(student);
            Logger.getLogger(this.getClass().getName()).info(stu.getsName());
            return Result.build(Const.HttpStatusCode.HttpStatus_200, "学生"+stu.getsName()+"信息修改成功");
        } else {
            return Result.build(Const.HttpStatusCode.HttpStatus_500, "学生信息修改失败");
        }
    }

    /**
     * 教师信息修改
     * */
    @ResponseBody
    @RequestMapping(value = "/teachers/info/modify")
    public Result modifyTeacher(@RequestBody Teacher teacher) {
        logger.info("teacher"+ teacher.toString());
        if (StringUtils.isEmpty(String.valueOf(teacher.getId()))) {
            return Result.build(Const.HttpStatusCode.HttpStatus_401, "教师编号不能为空");
        }
        if (userService.modifyTeacher(teacher)) {
            Teacher teac = userService.getTeacher(teacher);
            Logger.getLogger(this.getClass().getName()).info(teac.gettName());
            return Result.build(Const.HttpStatusCode.HttpStatus_200, "教师"+teac.gettName()+"信息修改成功");
        } else {
            return Result.build(Const.HttpStatusCode.HttpStatus_500, "教师信息修改失败");
        }
    }

    /**
     * 修改密码
     * @param user
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/modify/pwd")
    public Result modifyPwd(@RequestBody User user) {
        try {
            if (StringUtils.isEmpty(String.valueOf(user.getId()))) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "用户id不能为空");
            }
            if (StringUtils.isEmpty(user.getAccount())) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "旧密码不能为空");
            }
            if (StringUtils.isEmpty(user.getPassword())) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "新密码不能为空");
            }
            if (!userService.isPwdCorrect(user)) {
                return Result.build(Const.HttpStatusCode.HttpStatus_401, "旧密码不正确，请重新输入");
            }
            if (userService.modifyPwd(user)) {
                return Result.ok();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500();
        }
        return Result.fail_500();
    }

    /**
     * 查询所有学生
     */
    @RequestMapping(value = "/students/all")
    @ResponseBody
    public Result getStudentsAll() {
        try {
            List<Student> students = userService.getAllStudents();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("students", students);
            return Result.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500("students");
        }
    }

    /**
     * 查询所有教师
     */
    @RequestMapping(value = "/teachers/all")
    @ResponseBody
    public Result getTeachersAll() {
        try {
            List<Teacher> teachers = userService.getAllTeachers();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("teachers", teachers);
            return Result.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_array_500("teachers");
        }
    }

    /**
     * 教师用户删除
     */
    @RequestMapping(value = "/teachers/cancel")
    @ResponseBody
    public Result cancelTeacher(@RequestBody Teacher teacher) {
        if (teacher.getId() == null) {
            return Result.fail_500("教师id不能为空");
        }
        try {
            if (userService.cancelTeacher(teacher)) {
                return Result.ok("教师"+teacher.gettName()+"注销成功");
            } else {
                return Result.fail_500();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500(e.getMessage());
        }
    }

    /**
     * 学生用户注销
     */
    @RequestMapping(value = "/students/cancel")
    @ResponseBody
    public Result cancelStudent(@RequestBody Student student) {
        if (student.getStuId() == null) {
            return Result.fail_500("学生id不能为空");
        }
        try {
            if (userService.cancelStudent(student)) {
                return Result.ok("学生"+student.getsName()+"注销成功");
            } else {
                return Result.fail_500();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500(e.getMessage());
        }
    }

    /**
     * 学生用户开通
     */
    @RequestMapping(value = "/students/activate")
    @ResponseBody
    public Result activateStudent(@RequestBody Student student) {
        if (student.getStuId() == null) {
            return Result.fail_500("学生id不能为空");
        }
        try {
            if (userService.activateStudent(student)) {
                return Result.ok("学生"+student.getsName()+"开通成功");
            } else {
                return Result.fail_500();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail_500(e.getMessage());
        }
    }

//    class Wrapper {
//        private User user;
//        private Device device;
//
//        public Wrapper() {
//        }
//
//        public User getUser() {
//            return user;
//        }
//
//        public void setUser(User user) {
//            this.user = user;
//        }
//
//        public Device getDevice() {
//            return device;
//        }
//
//        public void setDevice(Device device) {
//            this.device = device;
//        }
//    }


}
