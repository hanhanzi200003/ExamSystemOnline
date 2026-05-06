package com.example.exam_system.login.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityValidator {

    // XSS攻击防护相关正则表达式
    private static final Pattern[] XSS_PATTERNS = {
            // Script标签
            Pattern.compile("<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE),
            // HTML事件属性 (需要更严格的匹配，避免误判)
            Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            // JavaScript伪协议
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // VBScript伪协议
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // Data URI (只检测危险类型)
            Pattern.compile("data:\\s*(text/html|application/javascript|image/svg\\+xml)", Pattern.CASE_INSENSITIVE),
            // 表达式
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
            // eval函数
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
            // document.cookie
            Pattern.compile("document\\.cookie", Pattern.CASE_INSENSITIVE),
            // innerHTML
            Pattern.compile("\\.innerHTML\\s*=", Pattern.CASE_INSENSITIVE),
            // src/href属性中的javascript
            Pattern.compile("(src|href)\\s*=\\s*[\"']\\s*javascript:", Pattern.CASE_INSENSITIVE)
    };

    // SQL注入防护相关正则表达式
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
            // SQL注释符号 (需要前后有空白或边界，避免误判正常文本中的--)
            Pattern.compile("(?<![\\w\\u4e00-\\u9fa5])--(?![\\w\\u4e00-\\u9fa5])", Pattern.CASE_INSENSITIVE),
            // 多行注释
            Pattern.compile("/\\*.*?\\*/", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // UNION SELECT
            Pattern.compile("union\\s+select", Pattern.CASE_INSENSITIVE),
            // DROP TABLE
            Pattern.compile("drop\\s+table", Pattern.CASE_INSENSITIVE),
            // DELETE FROM
            Pattern.compile("delete\\s+from", Pattern.CASE_INSENSITIVE),
            // INSERT INTO
            Pattern.compile("insert\\s+into", Pattern.CASE_INSENSITIVE),
            // UPDATE SET
            Pattern.compile("update\\s+.*?\\s+set", Pattern.CASE_INSENSITIVE),
            // EXECUTE
            Pattern.compile("exec(ute)?\\s+", Pattern.CASE_INSENSITIVE),
            // 系统表访问
            Pattern.compile("sys\\.(users|objects|columns)", Pattern.CASE_INSENSITIVE),
            // INFORMATION_SCHEMA
            Pattern.compile("information_schema", Pattern.CASE_INSENSITIVE),
            // xp_cmdshell等扩展存储过程
            Pattern.compile("xp_(cmdshell|regread|regwrite)", Pattern.CASE_INSENSITIVE),
            // OR 1=1 等恒真条件
            Pattern.compile("or\\s+1\\s*=\\s*1", Pattern.CASE_INSENSITIVE),
            Pattern.compile("or\\s+'1'\\s*=\\s*'1'", Pattern.CASE_INSENSITIVE),
            // AND 1=2 等恒假条件
            Pattern.compile("and\\s+1\\s*=\\s*2", Pattern.CASE_INSENSITIVE)
    };

    // 危险字符模式
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile("[<>\"'&]");

    /**
     * 全面的安全校验方法
     * @param input 输入字符串
     * @return 校验结果
     */
    public static ValidationResult validateInput(String input) {
        ValidationResult result = new ValidationResult();

        if (input == null || input.trim().isEmpty()) {
            result.setValid(false);
            result.addErrorMessage("输入不能为空");
            return result;
        }

        // 长度校验
        if (input.length() > 1000) {
            result.setValid(false);
            result.addErrorMessage("输入长度超过限制(1000字符)");
            return result;
        }

        // XSS攻击检测
        if (detectXSS(input)) {
            result.setValid(false);
            result.addErrorMessage("检测到潜在的XSS攻击代码");
            return result;
        }

        // SQL注入检测
        if (detectSQLInjection(input)) {
            result.setValid(false);
            result.addErrorMessage("检测到潜在的SQL注入代码");
            return result;
        }

        result.setValid(true);
        result.setSanitizedInput(sanitizeInput(input));
        return result;
    }

    /**
     * 专门的XSS防护校验
     * @param input 输入字符串
     * @return 是否安全
     */
    public static boolean isSafeFromXSS(String input) {
        return !detectXSS(input);
    }

    /**
     * 专门的SQL注入防护校验
     * @param input 输入字符串
     * @return 是否安全
     */
    public static boolean isSafeFromSQLInjection(String input) {
        return !detectSQLInjection(input);
    }

    /**
     * 输入净化处理
     * @param input 原始输入
     * @return 净化后的输入
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // HTML转义危险字符
        String sanitized = input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");

        // 移除危险的空白字符组合
        sanitized = sanitized.replaceAll("[\\r\\n\\t]+", " ");

        return sanitized.trim();
    }

    /**
     * 用户名安全校验（支持邮箱格式）
     * @param username 用户名
     * @return 校验结果
     */
    public static ValidationResult validateUsername(String username) {
        ValidationResult result = validateInput(username);
        if (!result.isValid()) {
            return result;
        }

        // 用户名特定规则 - 支持邮箱格式
        if (username.length() < 1 || username.length() > 100) {
            result.setValid(false);
            result.addErrorMessage("用户名长度应在1-100个字符之间");
            return result;
        }

        // 检查是否为邮箱格式
        boolean isEmailFormat = username.contains("@") && username.contains(".");

        if (isEmailFormat) {
            // 邮箱格式验证
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            if (!username.matches(emailRegex)) {
                result.setValid(false);
                result.addErrorMessage("邮箱格式不正确");
                return result;
            }
        } else {
            // 传统用户名格式验证
            if (username.length() < 3) {
                result.setValid(false);
                result.addErrorMessage("用户名长度至少3个字符");
                return result;
            }

            // 只允许字母、数字、下划线、中文（不包括特殊符号）
            if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
                result.setValid(false);
                result.addErrorMessage("用户名只能包含字母、数字、下划线和中文字符");
                return result;
            }
        }

        result.setSanitizedInput(username.trim());
        return result;
    }

    /**
     * 密码安全校验
     * @param password 密码
     * @return 校验结果
     */
    public static ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();

        if (password == null || password.isEmpty()) {
            result.setValid(false);
            result.addErrorMessage("密码不能为空");
            return result;
        }

        if (password.length() < 6 || password.length() > 24) {
            result.setValid(false);
            result.addErrorMessage("密码长度应在6-24个字符之间");
            return result;
        }

        String passwordRegex = "^[a-zA-Z0-9!@#$%^&*?]+$";
        if (!password.matches(passwordRegex)) {
            result.setValid(false);
            result.addErrorMessage("密码只能包含字母、数字和特殊符号(!@#$%^&*?)");
            return result;
        }

        String lowerPassword = password.toLowerCase();
        String[] weakPatterns = {
                "password", "123456", "admin", "root", "guest",
                "qwerty", "abc123", "111111", "000000"
        };

        for (String pattern : weakPatterns) {
            if (lowerPassword.contains(pattern)) {
                result.setValid(false);
                result.addErrorMessage("密码过于简单，请使用更复杂的密码");
                return result;
            }
        }

        result.setValid(true);
        result.setSanitizedInput(password);
        return result;
    }

    /**
     * 邮箱安全校验
     * @param email 邮箱
     * @return 校验结果
     */
    public static ValidationResult validateEmail(String email) {
        ValidationResult result = validateInput(email);
        if (!result.isValid()) {
            return result;
        }

        // 基本邮箱格式校验
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            result.setValid(false);
            result.addErrorMessage("邮箱格式不正确");
            return result;
        }

        result.setSanitizedInput(email.toLowerCase().trim());
        return result;
    }

    /**
     * 手机号安全校验
     * @param phone 手机号
     * @return 校验结果
     */
    public static ValidationResult validatePhone(String phone) {
        ValidationResult result = validateInput(phone);
        if (!result.isValid()) {
            return result;
        }

        // 中国手机号格式校验
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            result.setValid(false);
            result.addErrorMessage("手机号格式不正确");
            return result;
        }

        result.setSanitizedInput(phone);
        return result;
    }

    // 私有方法：检测XSS攻击
    private static boolean detectXSS(String input) {
        String lowerInput = input.toLowerCase();
        for (Pattern pattern : XSS_PATTERNS) {
            Matcher matcher = pattern.matcher(lowerInput);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    // 私有方法：检测SQL注入
    private static boolean detectSQLInjection(String input) {
        String lowerInput = input.toLowerCase();
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            Matcher matcher = pattern.matcher(lowerInput);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String sanitizedInput;
        private StringBuilder errorMessages;

        public ValidationResult() {
            this.valid = true;
            this.errorMessages = new StringBuilder();
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getSanitizedInput() {
            return sanitizedInput;
        }

        public void setSanitizedInput(String sanitizedInput) {
            this.sanitizedInput = sanitizedInput;
        }

        public String getErrorMessages() {
            return errorMessages.toString();
        }

        public void addErrorMessage(String message) {
            if (errorMessages.length() > 0) {
                errorMessages.append("; ");
            }
            errorMessages.append(message);
        }

        public boolean hasErrors() {
            return errorMessages.length() > 0;
        }
    }
}
