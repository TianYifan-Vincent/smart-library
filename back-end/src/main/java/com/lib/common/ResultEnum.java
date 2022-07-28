package com.lib.common;


public enum ResultEnum {
    //这里是可以自己定义的，方便与前端交互即可
    UNKNOWN_ERROR(-1,"未知错误"),
    USER_NOT_EXIST(1,"用户不存在"),
    USER_IS_EXISTS(2,"用户已存在"),
    DATA_IS_NULL(3,"数据为空"),
    USER_NOT_LOGIN(4,"用户未登录"),
    BAD_FORMAT(5,"格式错误"),
    WRONG_CODE(6,"密码错误"),
    NOT_NULLABLE(7,"数据不可非空"),
    WRONG_FORMAT_EMAIL(8,"邮箱格式错误"),
    WRONG_FORMAT_LOGIN_NAME(9,"用户名格式错误"),
    EMAIL_IS_EXISTS(10,"邮箱已存在"),
    LOGIN_NAME_IS_EXISTS(11,"账号已存在"),
    WRONG_FILEPATH(12,"文件路径错误"),
    ERROR_TRANSFER(13,"数据转换错误"),
    DATA_CACHE_DIF(14,"数据缓存不一致"),
    EMAIL_SEND_ERROR(15,"邮件发送失败"),
    EMAIL_SEND_SUCCESS(16,"邮件发送成功"),
    WRONG_VERIFY_CODE(17,"验证码错误"),
    EMAIL_NOT_EXISTS(18,"邮箱不存在"),
    VERIFY_CODE_NOT_FOUND(19,"验证码过期或不存在"),
    PAGE_BEYOND_LIMIT(20,"页面超出限制"),
    SUCCESS(200,"成功"),
    READER_LOGIN_SUCCESS(201,"读者成功"),
    ADMIN_LOGIN_SUCCESS(202,"管理员成功"),
    INSERT_ERROR(444,"插入失败"),
    CACHE_INSERT_ERROR(445,"Redis插入失败"),
    INSERT_SUCCESS(454,"插入成功"),
    QUERY_SUCCESS(455,"查询成功"),
    UPDATE_ERROR(456,"更新失败"),
    UPDATE_SUCCESS(457,"更新成功"),
    DELETE_ERROR(333,"删除失败"),
    DELETE_SUCCESS(334,"删除成功"),
    QUERY_ERROR(458,"查询失败"),



    READER_NOT_FINISH(700, "读者有借阅未归还"),
    CATEGORY_NOT_EXIST(701, "类别不存在"),
    ISBN_EXIST(702,"ISBN号已存在"),
    CATEGORY_IS_EXIST(703,"类别名已存在"),
    BOOK_IN_CATEGORY_EXIST(704,"该类别下存在书籍"),
    BOOK_COPY_ZERO(705,"书籍被借光"),
    VIO_TOO_MUCH(706,"违规次数过多"),
    BEYOND_MAX_BORROW(707,"超过最大借阅数"),
    OVERDUE_EXIST(708,"存在逾期未还书，请尽快归还"),
    APPROVE_EXIST(709,"审批已存在");






    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
