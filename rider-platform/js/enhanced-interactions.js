// 增强交互效果和用户体验
class EnhancedInteractions {
    constructor() {
        this.init();
    }

    init() {
        this.setupScrollProgress();
        this.setupBackToTop();
        this.setupIntersectionObserver();
        this.setupSmoothScrolling();
        this.setupParallaxEffects();
        this.setupNotificationSystem();
        this.setupTooltips();
        this.setupKeyboardNavigation();
        this.setupPerformanceOptimizations();
        this.setupMobileOptimizations();
    }

    // 滚动进度条
    setupScrollProgress() {
        const progressBar = document.createElement('div');
        progressBar.className = 'scroll-progress';
        document.body.appendChild(progressBar);

        window.addEventListener('scroll', () => {
            const scrolled = (window.scrollY / (document.documentElement.scrollHeight - window.innerHeight)) * 100;
            progressBar.style.width = `${Math.min(scrolled, 100)}%`;
        });
    }

    // 返回顶部按钮
    setupBackToTop() {
        const backToTopBtn = document.createElement('button');
        backToTopBtn.className = 'back-to-top';
        backToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
        backToTopBtn.setAttribute('aria-label', '返回顶部');
        document.body.appendChild(backToTopBtn);

        window.addEventListener('scroll', () => {
            if (window.scrollY > 300) {
                backToTopBtn.classList.add('visible');
            } else {
                backToTopBtn.classList.remove('visible');
            }
        });

        backToTopBtn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    // 交叉观察器 - 滚动动画
    setupIntersectionObserver() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    
                    // 为数字添加计数动画
                    if (entry.target.classList.contains('stat-number') || 
                        entry.target.classList.contains('data-value')) {
                        this.animateNumber(entry.target);
                    }
                    
                    // 为进度条添加动画
                    if (entry.target.classList.contains('progress-bar')) {
                        this.animateProgressBar(entry.target);
                    }
                }
            });
        }, observerOptions);

        // 观察所有需要动画的元素
        const animatedElements = document.querySelectorAll(
            '.fade-in, .slide-in-left, .slide-in-right, .scale-in, .rotate-in, ' +
            '.stat-number, .data-value, .progress-bar'
        );
        
        animatedElements.forEach(el => {
            observer.observe(el);
        });
    }

    // 数字计数动画
    animateNumber(element) {
        const target = parseInt(element.textContent.replace(/[^\d]/g, ''));
        const duration = 2000;
        const start = performance.now();
        const suffix = element.textContent.replace(/[\d]/g, '');

        const animate = (currentTime) => {
            const elapsed = currentTime - start;
            const progress = Math.min(elapsed / duration, 1);
            
            // 使用缓动函数
            const easeOutQuart = 1 - Math.pow(1 - progress, 4);
            const current = Math.floor(target * easeOutQuart);
            
            element.textContent = current + suffix;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }

    // 进度条动画
    animateProgressBar(element) {
        const targetWidth = element.getAttribute('data-width') || '100%';
        element.style.width = '0%';
        
        setTimeout(() => {
            element.style.transition = 'width 1.5s cubic-bezier(0.4, 0, 0.2, 1)';
            element.style.width = targetWidth;
        }, 200);
    }

    // 平滑滚动
    setupSmoothScrolling() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', (e) => {
                e.preventDefault();
                const target = document.querySelector(anchor.getAttribute('href'));
                
                if (target) {
                    const headerOffset = 80;
                    const elementPosition = target.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });
                }
            });
        });
    }

    // 视差效果
    setupParallaxEffects() {
        const parallaxElements = document.querySelectorAll('.floating-element, .orbit-icons');
        
        window.addEventListener('scroll', () => {
            const scrolled = window.pageYOffset;
            const rate = scrolled * -0.5;
            
            parallaxElements.forEach(element => {
                element.style.transform = `translateY(${rate}px)`;
            });
        });
    }

    // 通知系统
    setupNotificationSystem() {
        window.showNotification = (message, type = 'success', duration = 3000) => {
            const notification = document.createElement('div');
            notification.className = `notification ${type}`;
            notification.innerHTML = `
                <div style="display: flex; align-items: center; gap: 10px;">
                    <i class="fas fa-${this.getNotificationIcon(type)}"></i>
                    <span>${message}</span>
                    <button onclick="this.parentElement.parentElement.remove()" style="background: none; border: none; color: white; font-size: 18px; cursor: pointer; margin-left: auto;">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            `;
            
            document.body.appendChild(notification);
            
            // 显示动画
            setTimeout(() => notification.classList.add('show'), 100);
            
            // 自动隐藏
            setTimeout(() => {
                notification.classList.remove('show');
                setTimeout(() => notification.remove(), 400);
            }, duration);
        };
    }

    getNotificationIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    // 工具提示
    setupTooltips() {
        document.querySelectorAll('[data-tooltip]').forEach(element => {
            element.classList.add('tooltip');
        });
    }

    // 键盘导航
    setupKeyboardNavigation() {
        document.addEventListener('keydown', (e) => {
            // ESC 键关闭模态框
            if (e.key === 'Escape') {
                const activeModal = document.querySelector('.modal-overlay.active');
                if (activeModal) {
                    activeModal.classList.remove('active');
                }
            }
            
            // 空格键暂停/播放视频
            if (e.key === ' ' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                const videos = document.querySelectorAll('video');
                videos.forEach(video => {
                    if (video.paused) {
                        video.play();
                    } else {
                        video.pause();
                    }
                });
            }
        });
    }

    // 性能优化
    setupPerformanceOptimizations() {
        // 懒加载图片
        const images = document.querySelectorAll('img[data-src]');
        const imageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('skeleton');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        images.forEach(img => {
            img.classList.add('skeleton');
            imageObserver.observe(img);
        });

        // 防抖滚动事件
        let scrollTimeout;
        const originalScrollHandlers = [];
        
        window.addEventListener('scroll', () => {
            if (scrollTimeout) {
                clearTimeout(scrollTimeout);
            }
            
            scrollTimeout = setTimeout(() => {
                // 执行滚动相关的操作
                this.updateActiveNavItem();
            }, 10);
        });
    }

    // 更新活跃导航项
    updateActiveNavItem() {
        const sections = document.querySelectorAll('section[id]');
        const navLinks = document.querySelectorAll('.nav-menu a[href^="#"]');
        
        let current = '';
        sections.forEach(section => {
            const sectionTop = section.getBoundingClientRect().top;
            if (sectionTop <= 100) {
                current = section.getAttribute('id');
            }
        });
        
        navLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === `#${current}`) {
                link.classList.add('active');
            }
        });
    }

    // 移动端优化
    setupMobileOptimizations() {
        // 触摸反馈
        const touchElements = document.querySelectorAll('.btn, .story-card, .feature-card, .intro-card');
        
        touchElements.forEach(element => {
            element.addEventListener('touchstart', () => {
                element.style.transform = 'scale(0.98)';
            });
            
            element.addEventListener('touchend', () => {
                setTimeout(() => {
                    element.style.transform = '';
                }, 150);
            });
        });

        // 移动端菜单优化
        const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
        const navMenu = document.querySelector('.nav-menu');
        
        if (mobileMenuToggle && navMenu) {
            mobileMenuToggle.addEventListener('click', () => {
                mobileMenuToggle.classList.toggle('active');
                navMenu.classList.toggle('active');
                document.body.classList.toggle('menu-open');
            });
            
            // 点击菜单项后关闭菜单
            navMenu.querySelectorAll('a').forEach(link => {
                link.addEventListener('click', () => {
                    mobileMenuToggle.classList.remove('active');
                    navMenu.classList.remove('active');
                    document.body.classList.remove('menu-open');
                });
            });
        }

        // 防止移动端双击缩放
        let lastTouchEnd = 0;
        document.addEventListener('touchend', (e) => {
            const now = (new Date()).getTime();
            if (now - lastTouchEnd <= 300) {
                e.preventDefault();
            }
            lastTouchEnd = now;
        }, false);
    }

    // 添加动画类到元素
    addAnimationClasses() {
        // 为不同的元素添加不同的动画类
        const elementsToAnimate = [
            { selector: '.intro-card:nth-child(odd)', class: 'slide-in-left' },
            { selector: '.intro-card:nth-child(even)', class: 'slide-in-right' },
            { selector: '.story-card', class: 'fade-in' },
            { selector: '.feature-card', class: 'scale-in' },
            { selector: '.stat-item', class: 'rotate-in' },
            { selector: '.section-header', class: 'fade-in' }
        ];

        elementsToAnimate.forEach(({ selector, class: animationClass }) => {
            document.querySelectorAll(selector).forEach((element, index) => {
                element.classList.add(animationClass);
                element.style.animationDelay = `${index * 0.1}s`;
            });
        });
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    const enhancedInteractions = new EnhancedInteractions();
    enhancedInteractions.addAnimationClasses();
    
    // 显示加载完成通知
    setTimeout(() => {
        if (window.showNotification) {
            window.showNotification('网站加载完成，欢迎体验！', 'success', 2000);
        }
    }, 1000);
});

// 页面可见性变化时的优化
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        // 页面隐藏时暂停动画
        document.body.style.animationPlayState = 'paused';
    } else {
        // 页面显示时恢复动画
        document.body.style.animationPlayState = 'running';
    }
});

// 错误处理
window.addEventListener('error', (e) => {
    console.error('页面错误:', e.error);
    if (window.showNotification) {
        window.showNotification('页面出现错误，请刷新重试', 'error', 5000);
    }
});

// 网络状态监听
window.addEventListener('online', () => {
    if (window.showNotification) {
        window.showNotification('网络连接已恢复', 'success');
    }
});

window.addEventListener('offline', () => {
    if (window.showNotification) {
        window.showNotification('网络连接已断开', 'warning');
    }
});