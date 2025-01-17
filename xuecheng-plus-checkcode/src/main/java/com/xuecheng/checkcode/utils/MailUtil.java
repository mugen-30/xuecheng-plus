package com.xuecheng.checkcode.utils;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MailUtil {
    public static void main(String[] args) throws MessagingException {
        //可以在这里直接测试方法，填自己的邮箱即可
        sendTestMail("123@qq.com", achieveCode());
    }

    /**
     * 发送邮件
     * @param email 收件邮箱号
     * @param code  验证码
     * @throws MessagingException
     */
    public static void sendTestMail(String email, String code) throws MessagingException {
        // 创建Properties 类用于记录邮箱的一些属性
        Properties props = getProperties();
        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
        message.setFrom(form);
        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(email);
        message.setRecipient(RecipientType.TO, to);
        // 设置邮件标题
        message.setSubject("Mugen邮件测试");
        // 设置邮件的内容体
        message.setContent("尊敬的用户:你好!\n验证码为:" + code + "(有效期为30分钟,请勿告知他人)", "text/html;charset=UTF-8");
        // 最后当然就是发送邮件啦
        Transport.send(message);
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        // 表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        // 使用STARTTLS安全连接
        props.put("mail.smtp.starttls.enable", "true");
        //此处填写SMTP服务器
        props.put("mail.smtp.host", "smtp.qq.com");
        //端口号，QQ邮箱端口587
        props.put("mail.smtp.port", "587");
        // 此处填写，写信人的账号
        props.put("mail.user", "123@qq.com");
        // 此处填写16位STMP口令
        props.put("mail.password", "123");
        return props;
    }

    /**
     *  生成验证码
     * @return
     */
    public static String achieveCode() {  //由于数字 1 、 0 和字母 O 、l 有时分不清楚，所以，没有数字 1 、 0
        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List<String> list = Arrays.asList(beforeShuffle);//将数组转换为集合
        Collections.shuffle(list);  //打乱集合顺序
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s); //将集合转化为字符串
        }
        return sb.substring(3, 8);
    }
}
