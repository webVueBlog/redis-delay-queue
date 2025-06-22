// 联系我们模块的交互功能

// 复制到剪贴板功能
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showNotification('邮箱地址已复制到剪贴板', 'success');
    }).catch(() => {
        // 降级方案
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showNotification('邮箱地址已复制到剪贴板', 'success');
    });
}

// 打开地图功能
function openMap() {
    const address = '北京市朝阳区创新大厦';
    const encodedAddress = encodeURIComponent(address);
    const mapUrl = `https://map.baidu.com/search/${encodedAddress}`;
    window.open(mapUrl, '_blank');
}

// 表单字符计数功能
function initCharCounter() {
    const messageTextarea = document.getElementById('message');
    const charCount = document.getElementById('charCount');
    
    if (messageTextarea && charCount) {
        messageTextarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            charCount.textContent = currentLength;
            
            // 字符数接近限制时改变颜色
            if (currentLength > 450) {
                charCount.style.color = '#ff4444';
            } else if (currentLength > 400) {
                charCount.style.color = '#ffaa00';
            } else {
                charCount.style.color = '#666';
            }
        });
    }
}

// 表单验证和提交
function initContactForm() {
    const form = document.getElementById('contactForm');
    const formStatus = document.getElementById('formStatus');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // 获取表单数据
            const formData = new FormData(form);
            const data = {
                name: formData.get('name') || document.getElementById('name').value,
                email: formData.get('email') || document.getElementById('email').value,
                subject: formData.get('subject') || document.getElementById('subject').value,
                message: formData.get('message') || document.getElementById('message').value
            };
            
            // 验证表单
            if (!validateForm(data)) {
                return;
            }
            
            // 显示提交状态
            showFormStatus('正在发送消息...', 'loading');
            
            // 模拟发送请求
            setTimeout(() => {
                // 这里应该是实际的API调用
                submitContactForm(data);
            }, 1000);
        });
    }
}

// 表单验证
function validateForm(data) {
    const errors = [];
    
    if (!data.name || data.name.trim().length < 2) {
        errors.push('请输入有效的姓名');
    }
    
    if (!data.email || !isValidEmail(data.email)) {
        errors.push('请输入有效的邮箱地址');
    }
    
    if (!data.subject) {
        errors.push('请选择主题');
    }
    
    if (!data.message || data.message.trim().length < 10) {
        errors.push('留言内容至少需要10个字符');
    }
    
    if (data.message && data.message.length > 500) {
        errors.push('留言内容不能超过500个字符');
    }
    
    if (errors.length > 0) {
        showFormStatus(errors.join('；'), 'error');
        return false;
    }
    
    return true;
}

// 邮箱验证
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 提交表单
function submitContactForm(data) {
    // 这里应该是实际的API调用
    // 现在模拟成功响应
    console.log('提交的表单数据:', data);
    
    // 模拟成功
    showFormStatus('消息发送成功！我们会尽快回复您。', 'success');
    
    // 重置表单
    setTimeout(() => {
        document.getElementById('contactForm').reset();
        document.getElementById('charCount').textContent = '0';
        hideFormStatus();
    }, 3000);
}

// 显示表单状态
function showFormStatus(message, type) {
    const formStatus = document.getElementById('formStatus');
    if (formStatus) {
        formStatus.textContent = message;
        formStatus.className = `status-message ${type}`;
        formStatus.style.opacity = '1';
    }
}

// 隐藏表单状态
function hideFormStatus() {
    const formStatus = document.getElementById('formStatus');
    if (formStatus) {
        formStatus.style.opacity = '0';
        setTimeout(() => {
            formStatus.textContent = '';
            formStatus.className = 'status-message';
        }, 300);
    }
}

// 通知功能
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // 添加样式
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? 'rgba(0, 255, 136, 0.9)' : 'rgba(0, 123, 255, 0.9)'};
        color: #000;
        padding: 15px 20px;
        border-radius: 10px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        transform: translateX(100%);
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.2);
    `;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 显示动画
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // 自动隐藏
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// 浮动标签效果
function initFloatingLabels() {
    const inputs = document.querySelectorAll('.floating-label input, .floating-label select, .floating-label textarea');
    
    inputs.forEach(input => {
        // 检查初始值
        checkInputValue(input);
        
        // 监听输入事件
        input.addEventListener('input', () => checkInputValue(input));
        input.addEventListener('focus', () => checkInputValue(input));
        input.addEventListener('blur', () => checkInputValue(input));
    });
}

function checkInputValue(input) {
    const label = input.nextElementSibling;
    if (label && label.tagName === 'LABEL') {
        if (input.value.trim() !== '' || input === document.activeElement) {
            label.classList.add('active');
        } else {
            label.classList.remove('active');
        }
    }
}

// 联系方式悬停效果
function initContactItemEffects() {
    const contactItems = document.querySelectorAll('.contact-item');
    
    contactItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            const pulse = this.querySelector('.icon-pulse');
            if (pulse) {
                pulse.style.animationDuration = '1s';
            }
        });
        
        item.addEventListener('mouseleave', function() {
            const pulse = this.querySelector('.icon-pulse');
            if (pulse) {
                pulse.style.animationDuration = '2s';
            }
        });
    });
}

// 按钮涟漪效果
function initButtonRipple() {
    const buttons = document.querySelectorAll('.enhanced-btn');
    
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = this.querySelector('.btn-ripple');
            if (ripple) {
                ripple.style.width = '0';
                ripple.style.height = '0';
                
                setTimeout(() => {
                    ripple.style.width = '300px';
                    ripple.style.height = '300px';
                }, 10);
                
                setTimeout(() => {
                    ripple.style.width = '0';
                    ripple.style.height = '0';
                }, 600);
            }
        });
    });
}

// 初始化所有功能
function initContactModule() {
    initCharCounter();
    initContactForm();
    initFloatingLabels();
    initContactItemEffects();
    initButtonRipple();
    
    console.log('联系我们模块已初始化');
}

// 页面加载完成后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initContactModule);
} else {
    initContactModule();
}