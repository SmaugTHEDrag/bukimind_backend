<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if actionUri?has_content>
            <span style="font-size: 22px; font-weight: bold; color: #2563eb;">Xác nhận kích hoạt tài khoản</span>
        <#else>
            <span style="font-size: 22px; font-weight: bold; color: #10b981;">Xác thực thành công!</span>
        </#if>
    <#elseif section = "form">
        <div style="text-align: center; padding: 20px 10px;">
            <#if actionUri?has_content>
                <!-- Bước 1: Keycloak yêu cầu người dùng bấm để thực thi kích hoạt -->
                <div style="font-size: 48px; margin-bottom: 15px;">✉️</div>
                <p style="font-size: 16px; color: #334155; margin-bottom: 25px; line-height: 1.5;">
                    Vui lòng bấm vào nút bên dưới để hoàn tất việc xác thực tài khoản <strong>BukiMind</strong> của bạn.
                </p>
                <a href="${actionUri}"
                   style="display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 13px 32px; border-radius: 6px; font-weight: 600; font-size: 15px;">
                    Xác nhận kích hoạt ngay
                </a>
            <#else>
                <!-- Bước 2: Đã kích hoạt xong trong database Keycloak -->
                <div style="font-size: 48px; margin-bottom: 15px;">🎉</div>
                <p style="font-size: 16px; color: #334155; margin-bottom: 25px; line-height: 1.5;">
                    Địa chỉ email của bạn đã được xác nhận thành công. Tài khoản BukiMind hiện đã sẵn sàng để đăng nhập.
                </p>
                <a href="http://localhost:3000/login"
                   style="display: inline-block; background-color: #10b981; color: #ffffff; text-decoration: none; padding: 13px 32px; border-radius: 6px; font-weight: 600; font-size: 15px;">
                    Đến trang đăng nhập BukiMind
                </a>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>