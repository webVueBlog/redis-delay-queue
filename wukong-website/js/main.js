// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initLoading();
    initNavbar();
    initCharacterCards();
    initMediaTabs();
    initBackToTop();
    initScrollAnimations();
    initMobileMenu();
});

// 加载动画
function initLoading() {
    const loadingScreen = document.getElementById('loadingScreen');
    
    // 模拟加载过程
    setTimeout(() => {
        loadingScreen.classList.add('hidden');
        // 加载完成后移除元素
        setTimeout(() => {
            loadingScreen.remove();
        }, 500);
    }, 3000);
}

// 导航栏功能
function initNavbar() {
    const navbar = document.getElementById('navbar');
    const navLinks = document.querySelectorAll('.nav-link');
    
    // 滚动时改变导航栏样式
    window.addEventListener('scroll', () => {
        if (window.scrollY > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
    
    // 平滑滚动到对应章节
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                const offsetTop = targetElement.offsetTop - 80;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// 角色卡片交互
function initCharacterCards() {
    const characterCards = document.querySelectorAll('.character-card');
    const characterDetails = document.querySelectorAll('.character-detail');
    
    characterCards.forEach(card => {
        card.addEventListener('click', () => {
            // 移除所有活动状态
            characterCards.forEach(c => c.classList.remove('active'));
            characterDetails.forEach(d => d.classList.remove('active'));
            
            // 添加当前卡片的活动状态
            card.classList.add('active');
            
            // 显示对应的角色详情
            const character = card.getAttribute('data-character');
            const targetDetail = document.getElementById(character + '-detail');
            if (targetDetail) {
                targetDetail.classList.add('active');
            }
        });
    });
}

// 媒体标签页功能
function initMediaTabs() {
    const mediaTabs = document.querySelectorAll('.media-tab');
    const mediaPanels = document.querySelectorAll('.media-panel');
    
    mediaTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // 移除所有活动状态
            mediaTabs.forEach(t => t.classList.remove('active'));
            mediaPanels.forEach(p => p.classList.remove('active'));
            
            // 添加当前标签的活动状态
            tab.classList.add('active');
            
            // 显示对应的媒体面板
            const targetTab = tab.getAttribute('data-tab');
            const targetPanel = document.getElementById(targetTab);
            if (targetPanel) {
                targetPanel.classList.add('active');
            }
        });
    });
}

// 返回顶部按钮
function initBackToTop() {
    const backToTopBtn = document.getElementById('backToTop');
    
    // 滚动时显示/隐藏按钮
    window.addEventListener('scroll', () => {
        if (window.scrollY > 500) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });
    
    // 点击返回顶部
    backToTopBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// 滚动动画
function initScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // 观察需要动画的元素
    const animatedElements = document.querySelectorAll(
        '.section-header, .story-text, .story-visual, .feature-card, .news-card'
    );
    
    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
}

// 移动端菜单
function initMobileMenu() {
    const navToggle = document.getElementById('navToggle');
    const navMenu = document.querySelector('.nav-menu');
    
    if (navToggle && navMenu) {
        navToggle.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            navToggle.classList.toggle('active');
        });
        
        // 点击菜单项时关闭菜单
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                navMenu.classList.remove('active');
                navToggle.classList.remove('active');
            });
        });
    }
}

// 视频播放控制
function initVideoControls() {
    const videos = document.querySelectorAll('video');
    
    videos.forEach(video => {
        // 鼠标悬停时播放，离开时暂停
        video.addEventListener('mouseenter', () => {
            video.play();
        });
        
        video.addEventListener('mouseleave', () => {
            video.pause();
        });
    });
}

// 图片懒加载
function initLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => {
        imageObserver.observe(img);
    });
}

// 粒子效果（可选）
function initParticleEffect() {
    const hero = document.querySelector('.hero');
    const particleCount = 50;
    
    for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        particle.style.cssText = `
            position: absolute;
            width: 2px;
            height: 2px;
            background: #d4af37;
            border-radius: 50%;
            opacity: 0.5;
            animation: float ${Math.random() * 3 + 2}s infinite ease-in-out;
            left: ${Math.random() * 100}%;
            top: ${Math.random() * 100}%;
            animation-delay: ${Math.random() * 2}s;
        `;
        hero.appendChild(particle);
    }
    
    // 添加粒子动画CSS
    const style = document.createElement('style');
    style.textContent = `
        @keyframes float {
            0%, 100% {
                transform: translateY(0px) rotate(0deg);
                opacity: 0.5;
            }
            50% {
                transform: translateY(-20px) rotate(180deg);
                opacity: 1;
            }
        }
    `;
    document.head.appendChild(style);
}

// 音效控制（可选）
function initSoundEffects() {
    const buttons = document.querySelectorAll('button, .character-card, .media-item');
    
    buttons.forEach(button => {
        button.addEventListener('mouseenter', () => {
            // 播放悬停音效
            playSound('hover');
        });
        
        button.addEventListener('click', () => {
            // 播放点击音效
            playSound('click');
        });
    });
}

function playSound(type) {
    // 这里可以添加音效播放逻辑
    // const audio = new Audio(`sounds/${type}.mp3`);
    // audio.volume = 0.3;
    // audio.play();
}

// 主题切换（可选）
function initThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            document.body.classList.toggle('light-theme');
            localStorage.setItem('theme', 
                document.body.classList.contains('light-theme') ? 'light' : 'dark'
            );
        });
        
        // 加载保存的主题
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'light') {
            document.body.classList.add('light-theme');
        }
    }
}

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
    }
}

// 窗口大小改变时的处理
window.addEventListener('resize', debounce(() => {
    // 重新计算布局
    initScrollAnimations();
}, 250));

// 页面可见性变化处理
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        // 页面隐藏时暂停视频
        document.querySelectorAll('video').forEach(video => {
            video.pause();
        });
    }
});

// 错误处理
window.addEventListener('error', (e) => {
    console.error('页面错误:', e.error);
});

// 资源加载错误处理
document.addEventListener('error', (e) => {
    if (e.target.tagName === 'IMG') {
        e.target.src = 'images/placeholder.jpg'; // 设置默认图片
    }
}, true);

// 导出函数供其他脚本使用
window.WukongWebsite = {
    initLoading,
    initNavbar,
    initCharacterCards,
    initMediaTabs,
    initBackToTop,
    initScrollAnimations,
    playSound,
    debounce,
    throttle
};