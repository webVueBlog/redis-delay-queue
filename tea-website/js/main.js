// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 移动端导航菜单
    const mobileMenu = document.getElementById('mobile-menu');
    const navMenu = document.querySelector('.nav-menu');
    
    if (mobileMenu && navMenu) {
        mobileMenu.addEventListener('click', function() {
            mobileMenu.classList.toggle('active');
            navMenu.classList.toggle('active');
        });
    }

    // 导航链接点击事件
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                const offsetTop = targetSection.offsetTop - 70;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
            
            // 关闭移动端菜单
            if (navMenu.classList.contains('active')) {
                navMenu.classList.remove('active');
                mobileMenu.classList.remove('active');
            }
        });
    });

    // 滚动时导航栏效果
    const navbar = document.querySelector('.navbar');
    let lastScrollTop = 0;
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        if (scrollTop > 100) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.15)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
        }
        
        lastScrollTop = scrollTop;
    });

    // 滚动动画观察器
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, observerOptions);

    // 为需要动画的元素添加观察
    const animatedElements = document.querySelectorAll('.tea-type-card, .product-card, .feature, .contact-item');
    animatedElements.forEach(el => {
        el.classList.add('fade-in');
        observer.observe(el);
    });

    // 产品卡片悬停效果
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // 茶叶类型卡片点击效果
    const teaTypeCards = document.querySelectorAll('.tea-type-card');
    teaTypeCards.forEach(card => {
        card.addEventListener('click', function() {
            // 移除其他卡片的选中状态
            teaTypeCards.forEach(c => c.classList.remove('selected'));
            // 添加选中状态
            this.classList.add('selected');
            
            // 可以在这里添加筛选产品的逻辑
            const teaType = this.querySelector('h3').textContent;
            console.log('选择的茶叶类型:', teaType);
        });
    });

    // 联系表单处理
    const contactForm = document.querySelector('.contact-form');
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const name = this.querySelector('input[type="text"]').value;
            const email = this.querySelector('input[type="email"]').value;
            const message = this.querySelector('textarea').value;
            
            if (name && email && message) {
                // 这里可以添加实际的表单提交逻辑
                showNotification('消息发送成功！我们会尽快回复您。', 'success');
                this.reset();
            } else {
                showNotification('请填写所有必填字段。', 'error');
            }
        });
    }

    // 通知函数
    function showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        
        notification.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            background: ${type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : '#2196f3'};
            color: white;
            padding: 15px 20px;
            border-radius: 5px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 10000;
            transform: translateX(100%);
            transition: transform 0.3s ease;
        `;
        
        document.body.appendChild(notification);
        
        // 显示通知
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 100);
        
        // 隐藏通知
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }

    // 页面加载动画
    const heroText = document.querySelector('.hero-text');
    const heroImages = document.querySelector('.hero-images');
    
    if (heroText && heroImages) {
        heroText.style.opacity = '0';
        heroText.style.transform = 'translateY(50px)';
        heroImages.style.opacity = '0';
        heroImages.style.transform = 'translateX(50px)';
        
        setTimeout(() => {
            heroText.style.transition = 'all 0.8s ease';
            heroText.style.opacity = '1';
            heroText.style.transform = 'translateY(0)';
        }, 300);
        
        setTimeout(() => {
            heroImages.style.transition = 'all 0.8s ease';
            heroImages.style.opacity = '1';
            heroImages.style.transform = 'translateX(0)';
        }, 600);
    }

    // 返回顶部按钮
    const backToTopBtn = document.createElement('button');
    backToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
    backToTopBtn.className = 'back-to-top';
    backToTopBtn.style.cssText = `
        position: fixed;
        bottom: 30px;
        right: 30px;
        width: 50px;
        height: 50px;
        background: #4a7c59;
        color: white;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        display: none;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        box-shadow: 0 4px 12px rgba(74, 124, 89, 0.3);
        transition: all 0.3s ease;
        z-index: 1000;
    `;
    
    document.body.appendChild(backToTopBtn);
    
    // 显示/隐藏返回顶部按钮
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopBtn.style.display = 'flex';
        } else {
            backToTopBtn.style.display = 'none';
        }
    });
    
    // 返回顶部功能
    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    // 返回顶部按钮悬停效果
    backToTopBtn.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-3px)';
        this.style.boxShadow = '0 6px 20px rgba(74, 124, 89, 0.4)';
    });
    
    backToTopBtn.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0)';
        this.style.boxShadow = '0 4px 12px rgba(74, 124, 89, 0.3)';
    });

    // 图片懒加载和加载状态管理
    const images = document.querySelectorAll('img[data-src]');
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                loadImage(img);
                observer.unobserve(img);
            }
        });
    });

    // 图片加载函数
    function loadImage(img) {
        const src = img.dataset.src || img.src;
        if (!src) return;

        const newImg = new Image();
        
        newImg.onload = function() {
            img.src = src;
            img.classList.add('loaded');
            img.removeAttribute('data-src');
            
            // 添加淡入动画
            img.style.opacity = '0';
            setTimeout(() => {
                img.style.opacity = '1';
            }, 50);
        };
        
        newImg.onerror = function() {
            img.classList.add('error');
            img.alt = img.alt || '图片加载失败';
            // 使用本地占位符图片作为备选
            img.src = './images/placeholder.svg';
            console.warn('图片加载失败，使用占位符:', src);
        };
        
        newImg.src = src;
    }

    // 为所有图片添加加载状态管理
    const allImages = document.querySelectorAll('img');
    allImages.forEach(img => {
        if (img.dataset.src) {
            imageObserver.observe(img);
        } else if (img.src && !img.complete) {
            // 处理已有src但未加载完成的图片
            img.addEventListener('load', () => {
                img.classList.add('loaded');
            });
            img.addEventListener('error', () => {
                img.classList.add('error');
            });
        } else if (img.complete) {
            // 已加载完成的图片
            img.classList.add('loaded');
        }
    });

    // 预加载关键图片
    const criticalImages = [
        'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
        'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400'
    ];
    
    criticalImages.forEach(src => {
        const img = new Image();
        img.src = src;
    });

    // 性能优化：防抖函数
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // 性能优化：节流函数
    function throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    // 优化滚动事件
    const optimizedScrollHandler = throttle(function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        // 导航栏背景变化
        if (scrollTop > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    }, 16);

    window.addEventListener('scroll', optimizedScrollHandler);

    // 添加CSS类用于滚动状态
    const style = document.createElement('style');
    style.textContent = `
        .navbar.scrolled {
            background: rgba(255, 255, 255, 0.98) !important;
            box-shadow: 0 2px 20px rgba(0, 0, 0, 0.15) !important;
        }
        
        .tea-type-card.selected {
            background: linear-gradient(135deg, #4a7c59 0%, #2d5016 100%);
            color: white;
            transform: translateY(-5px);
        }
        
        .tea-type-card.selected .tea-circle {
            box-shadow: 0 8px 25px rgba(74, 124, 89, 0.3);
        }
        
        .tea-type-card.selected h3,
        .tea-type-card.selected p {
            color: white;
        }
    `;
    document.head.appendChild(style);

    console.log('茶叶网站初始化完成！');
});