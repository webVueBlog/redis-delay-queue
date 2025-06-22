// 全局变量
let scene, camera, renderer, controls;
let batterySwapStation, battery, robotArm;
let rider, electricBike, oldBattery;
let animationId;
let isAnimating = false;
let composer, bloomPass, filmPass, ssaoPass, unrealBloomPass;
let particleSystem, sparkles, energyField, hologramEffect;
let envMap, cubeCamera;
let riderSwapAnimation = false;
let dynamicLights = [];
let materialAnimations = [];
let time = 0;

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    initHero3D();
    initMain3D();
    initEventListeners();
    initScrollAnimations();
});

// 初始化首页3D场景
function initHero3D() {
    const container = document.getElementById('hero-3d-container');
    if (!container) return;

    // 创建场景
    const heroScene = new THREE.Scene();
    const heroCamera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    const heroRenderer = new THREE.WebGLRenderer({ 
        antialias: true, 
        alpha: true,
        powerPreference: "high-performance"
    });
    
    heroRenderer.setSize(container.clientWidth, container.clientHeight);
    heroRenderer.setClearColor(0x000000, 0);
    heroRenderer.shadowMap.enabled = true;
    heroRenderer.shadowMap.type = THREE.PCFSoftShadowMap;
    heroRenderer.outputEncoding = THREE.sRGBEncoding;
    heroRenderer.toneMapping = THREE.ACESFilmicToneMapping;
    heroRenderer.toneMappingExposure = 1.0;
    container.appendChild(heroRenderer.domElement);

    // 增强光照系统
    const ambientLight = new THREE.AmbientLight(0x404040, 0.4);
    heroScene.add(ambientLight);
    
    const directionalLight = new THREE.DirectionalLight(0xffffff, 1.0);
    directionalLight.position.set(10, 10, 5);
    directionalLight.castShadow = true;
    directionalLight.shadow.mapSize.width = 1024;
    directionalLight.shadow.mapSize.height = 1024;
    heroScene.add(directionalLight);
    
    // 蓝色点光源
    const pointLight = new THREE.PointLight(0x00d4ff, 0.8, 10);
    pointLight.position.set(2, 3, 2);
    heroScene.add(pointLight);

    // 创建增强的换电柜模型
    const stationGroup = new THREE.Group();
    
    // 主体结构 - PBR材质
    const mainBodyGeometry = new THREE.BoxGeometry(2, 3, 1);
    const mainBodyMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x2c3e50,
        roughness: 0.3,
        metalness: 0.7
    });
    const mainBody = new THREE.Mesh(mainBodyGeometry, mainBodyMaterial);
    mainBody.castShadow = true;
    mainBody.receiveShadow = true;
    stationGroup.add(mainBody);
    
    // 电池槽 - 发光效果
    for (let i = 0; i < 6; i++) {
        const slotGeometry = new THREE.BoxGeometry(0.3, 0.4, 0.2);
        const slotMaterial = new THREE.MeshStandardMaterial({ 
            color: 0x00d4ff,
            roughness: 0.1,
            metalness: 0.9,
            emissive: 0x001122,
            emissiveIntensity: 0.3
        });
        const slot = new THREE.Mesh(slotGeometry, slotMaterial);
        slot.position.set(-0.6 + (i % 3) * 0.6, 0.8 - Math.floor(i / 3) * 0.8, 0.6);
        slot.castShadow = true;
        slot.receiveShadow = true;
        
        // 添加发光边框
        const edgeGeometry = new THREE.EdgesGeometry(slotGeometry);
        const edgeMaterial = new THREE.LineBasicMaterial({ 
            color: 0x00ffff,
            transparent: true,
            opacity: 0.6
        });
        const edges = new THREE.LineSegments(edgeGeometry, edgeMaterial);
        slot.add(edges);
        
        stationGroup.add(slot);
    }
    
    // 显示屏 - 发光效果
    const screenGeometry = new THREE.PlaneGeometry(0.8, 0.5);
    const screenMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x0066cc,
        emissive: 0x002244,
        emissiveIntensity: 0.5,
        roughness: 0.1,
        metalness: 0.1
    });
    const screen = new THREE.Mesh(screenGeometry, screenMaterial);
    screen.position.set(0, 0.5, 0.51);
    stationGroup.add(screen);
    
    // 添加状态指示灯
    const indicators = [
        { pos: [-0.3, 1.2, 0.51], color: 0x00ff00 },
        { pos: [0, 1.2, 0.51], color: 0xffff00 },
        { pos: [0.3, 1.2, 0.51], color: 0xff0000 }
    ];
    
    indicators.forEach(indicator => {
        const indicatorGeometry = new THREE.SphereGeometry(0.03, 16, 16);
        const indicatorMaterial = new THREE.MeshStandardMaterial({
            color: indicator.color,
            emissive: indicator.color,
            emissiveIntensity: 0.4
        });
        const indicatorMesh = new THREE.Mesh(indicatorGeometry, indicatorMaterial);
        indicatorMesh.position.set(...indicator.pos);
        stationGroup.add(indicatorMesh);
    });
    
    heroScene.add(stationGroup);
    heroCamera.position.set(3, 2, 4);
    heroCamera.lookAt(0, 0, 0);

    // 动画循环
    function animateHero() {
        requestAnimationFrame(animateHero);
        
        // 旋转动画
        stationGroup.rotation.y += 0.005;
        
        // 指示灯闪烁
        const time = Date.now() * 0.003;
        stationGroup.children.forEach(child => {
            if (child.material && child.material.emissive) {
                const intensity = 0.3 + Math.sin(time + child.position.x) * 0.2;
                child.material.emissiveIntensity = intensity;
            }
        });
        
        heroRenderer.render(heroScene, heroCamera);
    }
    animateHero();

    // 响应式调整
    window.addEventListener('resize', () => {
        if (container.clientWidth > 0) {
            heroCamera.aspect = container.clientWidth / container.clientHeight;
            heroCamera.updateProjectionMatrix();
            heroRenderer.setSize(container.clientWidth, container.clientHeight);
        }
    });
}

// 初始化主要3D场景
function initMain3D() {
    const container = document.getElementById('three-container');
    if (!container) return;

    // 创建场景
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x1a1a1a);
    
    // 创建相机
    camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    camera.position.set(5, 5, 5);
    
    // 创建渲染器
    renderer = new THREE.WebGLRenderer({ 
        antialias: true,
        powerPreference: "high-performance",
        alpha: false
    });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    renderer.outputEncoding = THREE.sRGBEncoding;
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.2;
    renderer.physicallyCorrectLights = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    container.appendChild(renderer.domElement);
    
    // 添加控制器
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    
    // 创建环境贴图
    const cubeTextureLoader = new THREE.CubeTextureLoader();
    envMap = cubeTextureLoader.load([
        'data:image/svg+xml;base64,' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#1e3c72"/><stop offset="100%" stop-color="#2a5298"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>'),
        'data:image/svg+xml;base64,' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#1e3c72"/><stop offset="100%" stop-color="#2a5298"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>'),
        'data:image/svg+xml;base64=' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#667eea"/><stop offset="100%" stop-color="#764ba2"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>'),
        'data:image/svg+xml;base64=' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#2c3e50"/><stop offset="100%" stop-color="#3498db"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>'),
        'data:image/svg+xml;base64=' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#1e3c72"/><stop offset="100%" stop-color="#2a5298"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>'),
        'data:image/svg+xml;base64=' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#1e3c72"/><stop offset="100%" stop-color="#2a5298"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/></svg>')
    ]);
    scene.environment = envMap;
    scene.background = envMap;
    
    // 增强光照系统
    const ambientLight = new THREE.AmbientLight(0x404040, 0.3);
    scene.add(ambientLight);
    
    // 主光源
    const directionalLight = new THREE.DirectionalLight(0xffffff, 1.2);
    directionalLight.position.set(10, 15, 5);
    directionalLight.castShadow = true;
    directionalLight.shadow.mapSize.width = 4096;
    directionalLight.shadow.mapSize.height = 4096;
    directionalLight.shadow.camera.near = 0.1;
    directionalLight.shadow.camera.far = 50;
    directionalLight.shadow.camera.left = -10;
    directionalLight.shadow.camera.right = 10;
    directionalLight.shadow.camera.top = 10;
    directionalLight.shadow.camera.bottom = -10;
    directionalLight.shadow.bias = -0.0001;
    scene.add(directionalLight);
    
    // 蓝色点光源（科技感）
    const pointLight1 = new THREE.PointLight(0x00d4ff, 1.5, 15);
    pointLight1.position.set(0, 6, 3);
    pointLight1.castShadow = true;
    pointLight1.shadow.mapSize.width = 1024;
    pointLight1.shadow.mapSize.height = 1024;
    scene.add(pointLight1);
    
    // 橙色点光源（温暖感）
    const pointLight2 = new THREE.PointLight(0xff6600, 0.8, 12);
    pointLight2.position.set(-3, 4, -2);
    scene.add(pointLight2);
    
    // 聚光灯（重点照明）
    const spotLight = new THREE.SpotLight(0xffffff, 2, 20, Math.PI / 6, 0.3);
    spotLight.position.set(5, 8, 5);
    spotLight.target.position.set(0, 0, 0);
    spotLight.castShadow = true;
    spotLight.shadow.mapSize.width = 2048;
    spotLight.shadow.mapSize.height = 2048;
    scene.add(spotLight);
    scene.add(spotLight.target);
    
    // 创建高质量地面
    const groundGeometry = new THREE.PlaneGeometry(30, 30, 32, 32);
    const groundMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x2c3e50,
        roughness: 0.8,
        metalness: 0.2,
        envMap: envMap,
        envMapIntensity: 0.3
    });
    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
    ground.rotation.x = -Math.PI / 2;
    ground.receiveShadow = true;
    scene.add(ground);
    
    // 创建高级粒子系统
    createParticleSystem();
    
    // 创建闪烁效果
    createSparkles();
    
    // 创建能量场效果
    createEnergyField();
    
    // 创建全息投影效果
    createHologramEffect();
    
    // 创建动态光照系统
    createDynamicLights();
    
    // 创建换电柜
    createBatterySwapStation();
    
    // 创建电池
    createBattery();
    
    // 创建机械臂
    createRobotArm();
    
    // 创建骑手
    createRider();
    
    // 创建电动车
    createElectricBike();
    
    // 创建旧电池
    createOldBattery();
    
    // 初始化后处理效果
    initPostProcessing();
    
    // 创建材质动画（需要在模型创建后）
    createMaterialAnimations();
    
    // 开始渲染循环
    animate();
    
    // 响应式调整
    window.addEventListener('resize', onWindowResize);
}

// 创建换电柜模型
function createBatterySwapStation() {
    batterySwapStation = new THREE.Group();
    
    // 主体结构 - 使用PBR材质
    const mainGeometry = new THREE.BoxGeometry(3, 4, 2);
    const mainMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x2c3e50,
        roughness: 0.3,
        metalness: 0.8,
        envMap: envMap,
        envMapIntensity: 0.5
    });
    const mainBody = new THREE.Mesh(mainGeometry, mainMaterial);
    mainBody.position.y = 2;
    mainBody.castShadow = true;
    mainBody.receiveShadow = true;
    batterySwapStation.add(mainBody);
    
    // 电池存储槽
    const slotGroup = new THREE.Group();
    for (let row = 0; row < 3; row++) {
        for (let col = 0; col < 4; col++) {
            const slotGeometry = new THREE.BoxGeometry(0.4, 0.5, 0.3);
            const isActive = Math.random() > 0.3;
            const slotMaterial = new THREE.MeshStandardMaterial({ 
                color: isActive ? 0x00d4ff : 0x666666,
                roughness: isActive ? 0.1 : 0.8,
                metalness: isActive ? 0.9 : 0.3,
                emissive: isActive ? 0x001122 : 0x000000,
                emissiveIntensity: isActive ? 0.3 : 0,
                envMap: envMap,
                envMapIntensity: 0.4
            });
            const slot = new THREE.Mesh(slotGeometry, slotMaterial);
            slot.position.set(
                -1.2 + col * 0.8,
                1 + row * 0.8,
                1.1
            );
            slot.castShadow = true;
            slot.receiveShadow = true;
            
            // 为活跃槽位添加发光边框
            if (isActive) {
                const edgeGeometry = new THREE.EdgesGeometry(slotGeometry);
                const edgeMaterial = new THREE.LineBasicMaterial({ 
                    color: 0x00ffff,
                    transparent: true,
                    opacity: 0.8
                });
                const edges = new THREE.LineSegments(edgeGeometry, edgeMaterial);
                slot.add(edges);
            }
            
            slotGroup.add(slot);
        }
    }
    batterySwapStation.add(slotGroup);
    
    // 操作面板
    const panelGeometry = new THREE.BoxGeometry(1.5, 1, 0.1);
    const panelMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x000000,
        roughness: 0.1,
        metalness: 0.9,
        envMap: envMap,
        envMapIntensity: 0.3
    });
    const panel = new THREE.Mesh(panelGeometry, panelMaterial);
    panel.position.set(0, 1.5, 1.05);
    panel.castShadow = true;
    panel.receiveShadow = true;
    batterySwapStation.add(panel);
    
    // 显示屏 - 发光效果
    const screenGeometry = new THREE.PlaneGeometry(1.2, 0.7);
    const screenMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x0066cc,
        emissive: 0x002244,
        emissiveIntensity: 0.5,
        roughness: 0.1,
        metalness: 0.1
    });
    const screen = new THREE.Mesh(screenGeometry, screenMaterial);
    screen.position.set(0, 1.5, 1.11);
    batterySwapStation.add(screen);
    
    // 添加状态指示灯
    const indicatorGeometry = new THREE.SphereGeometry(0.05, 16, 16);
    const indicators = [
        { pos: [-0.5, 2.2, 1.11], color: 0x00ff00 }, // 绿色 - 运行
        { pos: [0, 2.2, 1.11], color: 0xffff00 },    // 黄色 - 待机
        { pos: [0.5, 2.2, 1.11], color: 0xff0000 }   // 红色 - 故障
    ];
    
    indicators.forEach(indicator => {
        const indicatorMaterial = new THREE.MeshStandardMaterial({
            color: indicator.color,
            emissive: indicator.color,
            emissiveIntensity: 0.3
        });
        const indicatorMesh = new THREE.Mesh(indicatorGeometry, indicatorMaterial);
        indicatorMesh.position.set(...indicator.pos);
        batterySwapStation.add(indicatorMesh);
    });
    
    scene.add(batterySwapStation);
}

// 创建电池模型
function createBattery() {
    battery = new THREE.Group();
    
    // 电池主体
    const batteryGeometry = new THREE.BoxGeometry(0.3, 0.4, 0.2);
    const batteryMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x00ff00,
        roughness: 0.2,
        metalness: 0.8,
        emissive: 0x002200,
        emissiveIntensity: 0.2,
        envMap: envMap,
        envMapIntensity: 0.6
    });
    const batteryBody = new THREE.Mesh(batteryGeometry, batteryMaterial);
    batteryBody.castShadow = true;
    batteryBody.receiveShadow = true;
    battery.add(batteryBody);
    
    // 电池正极
    const terminalGeometry = new THREE.CylinderGeometry(0.03, 0.03, 0.05);
    const terminalMaterial = new THREE.MeshStandardMaterial({
        color: 0xffd700,
        roughness: 0.1,
        metalness: 0.9,
        envMap: envMap
    });
    const terminal = new THREE.Mesh(terminalGeometry, terminalMaterial);
    terminal.position.set(0, 0.225, 0);
    battery.add(terminal);
    
    // 电量指示条
    const chargeGeometry = new THREE.PlaneGeometry(0.25, 0.05);
    const chargeMaterial = new THREE.MeshStandardMaterial({
        color: 0x00ff00,
        emissive: 0x004400,
        emissiveIntensity: 0.4,
        transparent: true,
        opacity: 0.8
    });
    const chargeIndicator = new THREE.Mesh(chargeGeometry, chargeMaterial);
    chargeIndicator.position.set(0, 0, 0.11);
    battery.add(chargeIndicator);
    
    battery.position.set(3, 0.2, 0);
    scene.add(battery);
}

// 创建机械臂模型
function createRobotArm() {
    robotArm = new THREE.Group();
    
    // 基座
    const baseGeometry = new THREE.CylinderGeometry(0.3, 0.3, 0.2);
    const baseMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x666666,
        roughness: 0.4,
        metalness: 0.7,
        envMap: envMap,
        envMapIntensity: 0.4
    });
    const base = new THREE.Mesh(baseGeometry, baseMaterial);
    base.position.y = 0.1;
    base.castShadow = true;
    base.receiveShadow = true;
    robotArm.add(base);
    
    // 第一段臂
    const arm1Geometry = new THREE.BoxGeometry(0.1, 1.5, 0.1);
    const armMaterial = new THREE.MeshStandardMaterial({ 
        color: 0xff6600,
        roughness: 0.3,
        metalness: 0.6,
        emissive: 0x221100,
        emissiveIntensity: 0.1,
        envMap: envMap,
        envMapIntensity: 0.5
    });
    const arm1 = new THREE.Mesh(arm1Geometry, armMaterial);
    arm1.position.y = 0.95;
    arm1.castShadow = true;
    arm1.receiveShadow = true;
    robotArm.add(arm1);
    
    // 关节
    const jointGeometry = new THREE.SphereGeometry(0.08, 16, 16);
    const jointMaterial = new THREE.MeshStandardMaterial({
        color: 0x444444,
        roughness: 0.2,
        metalness: 0.9,
        envMap: envMap
    });
    const joint = new THREE.Mesh(jointGeometry, jointMaterial);
    joint.position.y = 1.7;
    joint.castShadow = true;
    robotArm.add(joint);
    
    // 第二段臂
    const arm2Geometry = new THREE.BoxGeometry(1, 0.1, 0.1);
    const arm2 = new THREE.Mesh(arm2Geometry, armMaterial);
    arm2.position.set(0.5, 1.7, 0);
    arm2.castShadow = true;
    arm2.receiveShadow = true;
    robotArm.add(arm2);
    
    // 抓手
    const gripperGeometry = new THREE.BoxGeometry(0.2, 0.2, 0.2);
    const gripperMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x333333,
        roughness: 0.1,
        metalness: 0.9,
        envMap: envMap,
        envMapIntensity: 0.6
    });
    const gripper = new THREE.Mesh(gripperGeometry, gripperMaterial);
    gripper.position.set(1, 1.7, 0);
    gripper.castShadow = true;
    gripper.receiveShadow = true;
    robotArm.add(gripper);
    
    // 抓手爪子
    const clawGeometry = new THREE.BoxGeometry(0.05, 0.15, 0.03);
    const clawMaterial = new THREE.MeshStandardMaterial({
        color: 0x555555,
        roughness: 0.2,
        metalness: 0.8
    });
    
    const claw1 = new THREE.Mesh(clawGeometry, clawMaterial);
    claw1.position.set(1.1, 1.7, 0.08);
    robotArm.add(claw1);
    
    const claw2 = new THREE.Mesh(clawGeometry, clawMaterial);
    claw2.position.set(1.1, 1.7, -0.08);
    robotArm.add(claw2);
    
    robotArm.position.set(-2, 0, 0);
    scene.add(robotArm);
}

// 创建骑手模型
function createRider() {
    rider = new THREE.Group();
    
    // 身体
    const bodyGeometry = new THREE.CylinderGeometry(0.15, 0.2, 0.8);
    const bodyMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x4169e1,
        roughness: 0.6,
        metalness: 0.1
    });
    const body = new THREE.Mesh(bodyGeometry, bodyMaterial);
    body.position.y = 1.2;
    body.castShadow = true;
    rider.add(body);
    
    // 头部
    const headGeometry = new THREE.SphereGeometry(0.12);
    const headMaterial = new THREE.MeshStandardMaterial({ 
        color: 0xffdbac,
        roughness: 0.8,
        metalness: 0.0
    });
    const head = new THREE.Mesh(headGeometry, headMaterial);
    head.position.y = 1.72;
    head.castShadow = true;
    rider.add(head);
    
    // 头盔
    const helmetGeometry = new THREE.SphereGeometry(0.14);
    const helmetMaterial = new THREE.MeshStandardMaterial({ 
        color: 0xff4500,
        roughness: 0.3,
        metalness: 0.7
    });
    const helmet = new THREE.Mesh(helmetGeometry, helmetMaterial);
    helmet.position.y = 1.75;
    helmet.scale.y = 0.8;
    helmet.castShadow = true;
    rider.add(helmet);
    
    // 左臂
    const armGeometry = new THREE.CylinderGeometry(0.04, 0.05, 0.5);
    const armMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x4169e1,
        roughness: 0.6,
        metalness: 0.1
    });
    const leftArm = new THREE.Mesh(armGeometry, armMaterial);
    leftArm.position.set(-0.2, 1.35, 0);
    leftArm.rotation.z = 0.3;
    leftArm.castShadow = true;
    rider.add(leftArm);
    
    // 右臂
    const rightArm = new THREE.Mesh(armGeometry, armMaterial);
    rightArm.position.set(0.2, 1.35, 0);
    rightArm.rotation.z = -0.3;
    rightArm.castShadow = true;
    rider.add(rightArm);
    
    // 左腿
    const legGeometry = new THREE.CylinderGeometry(0.05, 0.06, 0.6);
    const legMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x2f4f4f,
        roughness: 0.7,
        metalness: 0.1
    });
    const leftLeg = new THREE.Mesh(legGeometry, legMaterial);
    leftLeg.position.set(-0.1, 0.5, 0);
    leftLeg.castShadow = true;
    rider.add(leftLeg);
    
    // 右腿
    const rightLeg = new THREE.Mesh(legGeometry, legMaterial);
    rightLeg.position.set(0.1, 0.5, 0);
    rightLeg.castShadow = true;
    rider.add(rightLeg);
    
    // 背包
    const backpackGeometry = new THREE.BoxGeometry(0.25, 0.3, 0.15);
    const backpackMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x228b22,
        roughness: 0.8,
        metalness: 0.1
    });
    const backpack = new THREE.Mesh(backpackGeometry, backpackMaterial);
    backpack.position.set(0, 1.3, -0.2);
    backpack.castShadow = true;
    rider.add(backpack);
    
    rider.position.set(5, 0, 2);
    scene.add(rider);
}

// 创建电动车模型
function createElectricBike() {
    electricBike = new THREE.Group();
    
    // 车身
    const frameGeometry = new THREE.BoxGeometry(1.2, 0.1, 0.4);
    const frameMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x1e90ff,
        roughness: 0.3,
        metalness: 0.8
    });
    const frame = new THREE.Mesh(frameGeometry, frameMaterial);
    frame.position.y = 0.4;
    frame.castShadow = true;
    electricBike.add(frame);
    
    // 前轮
    const wheelGeometry = new THREE.CylinderGeometry(0.25, 0.25, 0.1);
    const wheelMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x2f2f2f,
        roughness: 0.9,
        metalness: 0.1
    });
    const frontWheel = new THREE.Mesh(wheelGeometry, wheelMaterial);
    frontWheel.position.set(0.5, 0.25, 0);
    frontWheel.rotation.x = Math.PI / 2;
    frontWheel.castShadow = true;
    electricBike.add(frontWheel);
    
    // 后轮
    const rearWheel = new THREE.Mesh(wheelGeometry, wheelMaterial);
    rearWheel.position.set(-0.5, 0.25, 0);
    rearWheel.rotation.x = Math.PI / 2;
    rearWheel.castShadow = true;
    electricBike.add(rearWheel);
    
    // 座椅
    const seatGeometry = new THREE.BoxGeometry(0.3, 0.05, 0.2);
    const seatMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x8b4513,
        roughness: 0.8,
        metalness: 0.1
    });
    const seat = new THREE.Mesh(seatGeometry, seatMaterial);
    seat.position.set(-0.2, 0.55, 0);
    seat.castShadow = true;
    electricBike.add(seat);
    
    // 把手
    const handleGeometry = new THREE.CylinderGeometry(0.02, 0.02, 0.4);
    const handleMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x696969,
        roughness: 0.4,
        metalness: 0.8
    });
    const handle = new THREE.Mesh(handleGeometry, handleMaterial);
    handle.position.set(0.4, 0.7, 0);
    handle.rotation.z = Math.PI / 2;
    handle.castShadow = true;
    electricBike.add(handle);
    
    // 电池仓（空的）
    const batteryCompartmentGeometry = new THREE.BoxGeometry(0.35, 0.45, 0.25);
    const batteryCompartmentMaterial = new THREE.MeshStandardMaterial({ 
        color: 0x333333,
        roughness: 0.5,
        metalness: 0.6,
        transparent: true,
        opacity: 0.8
    });
    const batteryCompartment = new THREE.Mesh(batteryCompartmentGeometry, batteryCompartmentMaterial);
    batteryCompartment.position.set(0, 0.6, 0);
    batteryCompartment.castShadow = true;
    electricBike.add(batteryCompartment);
    
    electricBike.position.set(4, 0, 2);
    scene.add(electricBike);
}

// 创建旧电池模型
function createOldBattery() {
    oldBattery = new THREE.Group();
    
    // 电池主体（红色表示电量低）
    const batteryGeometry = new THREE.BoxGeometry(0.3, 0.4, 0.2);
    const batteryMaterial = new THREE.MeshStandardMaterial({ 
        color: 0xff4444,
        roughness: 0.2,
        metalness: 0.8,
        emissive: 0x220000,
        emissiveIntensity: 0.3
    });
    const batteryBody = new THREE.Mesh(batteryGeometry, batteryMaterial);
    batteryBody.castShadow = true;
    batteryBody.receiveShadow = true;
    oldBattery.add(batteryBody);
    
    // 电池正极
    const terminalGeometry = new THREE.CylinderGeometry(0.03, 0.03, 0.05);
    const terminalMaterial = new THREE.MeshStandardMaterial({
        color: 0xffd700,
        roughness: 0.1,
        metalness: 0.9
    });
    const terminal = new THREE.Mesh(terminalGeometry, terminalMaterial);
    terminal.position.set(0, 0.225, 0);
    oldBattery.add(terminal);
    
    // 低电量指示条（红色）
    const chargeGeometry = new THREE.PlaneGeometry(0.25, 0.05);
    const chargeMaterial = new THREE.MeshStandardMaterial({
        color: 0xff0000,
        emissive: 0x440000,
        emissiveIntensity: 0.6,
        transparent: true,
        opacity: 0.9
    });
    const chargeIndicator = new THREE.Mesh(chargeGeometry, chargeMaterial);
    chargeIndicator.position.set(0, 0, 0.11);
    oldBattery.add(chargeIndicator);
    
    // 初始位置在电动车的电池仓内
    oldBattery.position.set(4, 0.6, 2);
    scene.add(oldBattery);
}

// 创建高级粒子系统
function createParticleSystem() {
    const particleCount = 2000;
    const particles = new THREE.BufferGeometry();
    const positions = new Float32Array(particleCount * 3);
    const colors = new Float32Array(particleCount * 3);
    const sizes = new Float32Array(particleCount);
    const velocities = new Float32Array(particleCount * 3);
    const lifetimes = new Float32Array(particleCount);
    
    for (let i = 0; i < particleCount; i++) {
        // 在换电柜周围分布粒子
        const radius = 5 + Math.random() * 10;
        const theta = Math.random() * Math.PI * 2;
        const phi = Math.random() * Math.PI;
        
        positions[i * 3] = radius * Math.sin(phi) * Math.cos(theta);
        positions[i * 3 + 1] = radius * Math.cos(phi);
        positions[i * 3 + 2] = radius * Math.sin(phi) * Math.sin(theta);
        
        // 多彩能量粒子
        const colorType = Math.random();
        if (colorType < 0.4) {
            // 蓝色能量
            colors[i * 3] = 0.1 + Math.random() * 0.3;
            colors[i * 3 + 1] = 0.5 + Math.random() * 0.5;
            colors[i * 3 + 2] = 1.0;
        } else if (colorType < 0.7) {
            // 青色能量
            colors[i * 3] = 0.0;
            colors[i * 3 + 1] = 0.8 + Math.random() * 0.2;
            colors[i * 3 + 2] = 0.8 + Math.random() * 0.2;
        } else {
            // 紫色能量
            colors[i * 3] = 0.6 + Math.random() * 0.4;
            colors[i * 3 + 1] = 0.2 + Math.random() * 0.3;
            colors[i * 3 + 2] = 1.0;
        }
        
        sizes[i] = Math.random() * 3 + 0.5;
        
        // 粒子运动速度
        velocities[i * 3] = (Math.random() - 0.5) * 0.02;
        velocities[i * 3 + 1] = (Math.random() - 0.5) * 0.02;
        velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.02;
        
        lifetimes[i] = Math.random() * 100;
    }
    
    particles.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    particles.setAttribute('color', new THREE.BufferAttribute(colors, 3));
    particles.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
    particles.setAttribute('velocity', new THREE.BufferAttribute(velocities, 3));
    particles.setAttribute('lifetime', new THREE.BufferAttribute(lifetimes, 1));
    
    const particleMaterial = new THREE.PointsMaterial({
        size: 0.15,
        vertexColors: true,
        transparent: true,
        opacity: 0.8,
        blending: THREE.AdditiveBlending,
        sizeAttenuation: true
    });
    
    particleSystem = new THREE.Points(particles, particleMaterial);
    scene.add(particleSystem);
}

// 创建能量场效果
function createEnergyField() {
    const fieldGeometry = new THREE.SphereGeometry(8, 32, 32);
    const fieldMaterial = new THREE.ShaderMaterial({
        uniforms: {
            time: { value: 0 },
            color1: { value: new THREE.Color(0x00d4ff) },
            color2: { value: new THREE.Color(0xff0096) },
            opacity: { value: 0.1 }
        },
        vertexShader: `
            varying vec2 vUv;
            varying vec3 vPosition;
            void main() {
                vUv = uv;
                vPosition = position;
                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
            }
        `,
        fragmentShader: `
            uniform float time;
            uniform vec3 color1;
            uniform vec3 color2;
            uniform float opacity;
            varying vec2 vUv;
            varying vec3 vPosition;
            
            void main() {
                float noise = sin(vPosition.x * 0.5 + time) * sin(vPosition.y * 0.3 + time * 1.2) * sin(vPosition.z * 0.4 + time * 0.8);
                vec3 color = mix(color1, color2, noise * 0.5 + 0.5);
                float alpha = opacity * (0.5 + 0.5 * noise);
                gl_FragColor = vec4(color, alpha);
            }
        `,
        transparent: true,
        blending: THREE.AdditiveBlending,
        side: THREE.DoubleSide
    });
    
    energyField = new THREE.Mesh(fieldGeometry, fieldMaterial);
    energyField.position.set(0, 0, 0);
    scene.add(energyField);
}

// 创建全息投影效果
function createHologramEffect() {
    const holoGeometry = new THREE.PlaneGeometry(3, 2);
    const holoMaterial = new THREE.ShaderMaterial({
        uniforms: {
            time: { value: 0 },
            scanlineIntensity: { value: 0.5 },
            glitchIntensity: { value: 0.1 }
        },
        vertexShader: `
            varying vec2 vUv;
            uniform float time;
            void main() {
                vUv = uv;
                vec3 pos = position;
                pos.x += sin(time * 2.0 + uv.y * 10.0) * 0.02;
                gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
            }
        `,
        fragmentShader: `
            uniform float time;
            uniform float scanlineIntensity;
            uniform float glitchIntensity;
            varying vec2 vUv;
            
            void main() {
                vec2 uv = vUv;
                
                // 扫描线效果
                float scanline = sin(uv.y * 100.0 + time * 5.0) * scanlineIntensity;
                
                // 故障效果
                float glitch = step(0.98, sin(time * 10.0)) * glitchIntensity;
                uv.x += glitch * (sin(time * 50.0) * 0.1);
                
                // 全息颜色
                vec3 color = vec3(0.0, 0.8, 1.0);
                color += scanline;
                
                // 边缘发光
                float edge = 1.0 - smoothstep(0.0, 0.1, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
                color += edge * 0.5;
                
                float alpha = 0.7 + scanline * 0.3;
                gl_FragColor = vec4(color, alpha);
            }
        `,
        transparent: true,
        blending: THREE.AdditiveBlending,
        side: THREE.DoubleSide
    });
    
    hologramEffect = new THREE.Mesh(holoGeometry, holoMaterial);
    hologramEffect.position.set(4, 2, 0);
    hologramEffect.rotation.y = -Math.PI / 4;
    scene.add(hologramEffect);
}

// 创建动态光照系统
function createDynamicLights() {
    // 动态彩色点光源
    for (let i = 0; i < 5; i++) {
        const light = new THREE.PointLight(0xffffff, 0.5, 10);
        const angle = (i / 5) * Math.PI * 2;
        light.position.set(
            Math.cos(angle) * 6,
            2 + Math.sin(i) * 2,
            Math.sin(angle) * 6
        );
        
        // 随机颜色
        const colors = [0x00d4ff, 0xff0096, 0x00ff88, 0xffaa00, 0x8800ff];
        light.color.setHex(colors[i]);
        
        // 添加光源辅助器（可选，用于调试）
        // const lightHelper = new THREE.PointLightHelper(light, 0.2);
        // scene.add(lightHelper);
        
        dynamicLights.push({
            light: light,
            originalPosition: light.position.clone(),
            phase: i * Math.PI * 0.4,
            speed: 0.5 + Math.random() * 0.5
        });
        
        scene.add(light);
    }
    
    // 聚光灯效果
    const spotLight = new THREE.SpotLight(0x00d4ff, 1, 20, Math.PI / 6, 0.5);
    spotLight.position.set(0, 10, 5);
    spotLight.target.position.set(0, 0, 0);
    spotLight.castShadow = true;
    spotLight.shadow.mapSize.width = 1024;
    spotLight.shadow.mapSize.height = 1024;
    scene.add(spotLight);
    scene.add(spotLight.target);
    
    dynamicLights.push({
        light: spotLight,
        type: 'spotlight',
        phase: 0,
        speed: 0.3
    });
}

// 创建材质动画系统
function createMaterialAnimations() {
    // 为换电柜添加发光材质动画
    if (batterySwapStation) {
        batterySwapStation.traverse((child) => {
            if (child.isMesh && child.material) {
                const originalEmissive = child.material.emissive ? child.material.emissive.clone() : new THREE.Color(0x000000);
                const originalEmissiveIntensity = child.material.emissiveIntensity || 0;
                
                materialAnimations.push({
                    material: child.material,
                    originalEmissive: originalEmissive,
                    originalEmissiveIntensity: originalEmissiveIntensity,
                    phase: Math.random() * Math.PI * 2,
                    speed: 0.5 + Math.random() * 1.0
                });
            }
        });
    }
}

// 创建闪烁效果
function createSparkles() {
    sparkles = new THREE.Group();
    
    for (let i = 0; i < 20; i++) {
        const sparkleGeometry = new THREE.SphereGeometry(0.02, 8, 8);
        const sparkleMaterial = new THREE.MeshStandardMaterial({
            color: 0x00ffff,
            emissive: 0x00ffff,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.8
        });
        
        const sparkle = new THREE.Mesh(sparkleGeometry, sparkleMaterial);
        sparkle.position.set(
            (Math.random() - 0.5) * 10,
            Math.random() * 8,
            (Math.random() - 0.5) * 10
        );
        
        sparkle.userData = {
            originalY: sparkle.position.y,
            speed: 0.01 + Math.random() * 0.02,
            amplitude: 0.5 + Math.random() * 1
        };
        
        sparkles.add(sparkle);
    }
    
    scene.add(sparkles);
}

// 初始化后处理效果
function initPostProcessing() {
    // 创建环境立方体相机用于实时反射
    cubeCamera = new THREE.CubeCamera(0.1, 1000, 256);
    scene.add(cubeCamera);
    
    // 设置高级后处理
    composer = new THREE.EffectComposer(renderer);
    const renderPass = new THREE.RenderPass(scene, camera);
    composer.addPass(renderPass);
    
    // SSAO环境光遮蔽效果
    if (THREE.SSAOPass) {
        ssaoPass = new THREE.SSAOPass(scene, camera, window.innerWidth, window.innerHeight);
        ssaoPass.kernelRadius = 16;
        ssaoPass.minDistance = 0.005;
        ssaoPass.maxDistance = 0.1;
        composer.addPass(ssaoPass);
    }
    
    // 增强辉光效果
    unrealBloomPass = new THREE.UnrealBloomPass(
        new THREE.Vector2(window.innerWidth, window.innerHeight),
        2.0, 0.6, 0.7
    );
    unrealBloomPass.threshold = 0.1;
    unrealBloomPass.strength = 1.5;
    unrealBloomPass.radius = 0.8;
    composer.addPass(unrealBloomPass);
    
    // 胶片颗粒效果
    filmPass = new THREE.FilmPass(0.2, 0.015, 648, false);
    composer.addPass(filmPass);
    
    // 色调映射增强
    const gammaCorrectionPass = new THREE.ShaderPass(THREE.GammaCorrectionShader);
    composer.addPass(gammaCorrectionPass);
}

// 动画循环
function animate() {
    animationId = requestAnimationFrame(animate);
    
    time += 0.016; // 假设60fps
    
    // 更新控制器
    controls.update();
    
    // 更新环境立方体相机
    if (cubeCamera) {
        cubeCamera.update(renderer, scene);
    }
    
    // 高级粒子系统动画
    if (particleSystem) {
        particleSystem.rotation.y += 0.002;
        const positions = particleSystem.geometry.attributes.position.array;
        const velocities = particleSystem.geometry.attributes.velocity.array;
        const lifetimes = particleSystem.geometry.attributes.lifetime.array;
        const colors = particleSystem.geometry.attributes.color.array;
        
        for (let i = 0; i < positions.length; i += 3) {
            // 粒子运动
            positions[i] += velocities[i];
            positions[i + 1] += velocities[i + 1];
            positions[i + 2] += velocities[i + 2];
            
            // 生命周期
            lifetimes[i / 3] -= 0.5;
            if (lifetimes[i / 3] <= 0) {
                // 重置粒子
                const radius = 5 + Math.random() * 10;
                const theta = Math.random() * Math.PI * 2;
                const phi = Math.random() * Math.PI;
                
                positions[i] = radius * Math.sin(phi) * Math.cos(theta);
                positions[i + 1] = radius * Math.cos(phi);
                positions[i + 2] = radius * Math.sin(phi) * Math.sin(theta);
                
                lifetimes[i / 3] = Math.random() * 100;
                
                // 重新设置颜色
                const colorType = Math.random();
                if (colorType < 0.4) {
                    colors[i] = 0.1 + Math.random() * 0.3;
                    colors[i + 1] = 0.5 + Math.random() * 0.5;
                    colors[i + 2] = 1.0;
                } else if (colorType < 0.7) {
                    colors[i] = 0.0;
                    colors[i + 1] = 0.8 + Math.random() * 0.2;
                    colors[i + 2] = 0.8 + Math.random() * 0.2;
                } else {
                    colors[i] = 0.6 + Math.random() * 0.4;
                    colors[i + 1] = 0.2 + Math.random() * 0.3;
                    colors[i + 2] = 1.0;
                }
            }
        }
        
        particleSystem.geometry.attributes.position.needsUpdate = true;
        particleSystem.geometry.attributes.color.needsUpdate = true;
    }
    
    // 能量场动画
    if (energyField && energyField.material.uniforms) {
        energyField.material.uniforms.time.value = time;
        energyField.rotation.y += 0.005;
        energyField.rotation.x = Math.sin(time * 0.3) * 0.1;
    }
    
    // 全息投影动画
    if (hologramEffect && hologramEffect.material.uniforms) {
        hologramEffect.material.uniforms.time.value = time;
        hologramEffect.position.y = 2 + Math.sin(time * 2) * 0.1;
        hologramEffect.rotation.y += 0.01;
    }
    
    // 动态光照动画
    dynamicLights.forEach((lightData, index) => {
        if (lightData.type === 'spotlight') {
            // 聚光灯旋转
            lightData.phase += lightData.speed * 0.016;
            const angle = lightData.phase;
            lightData.light.position.x = Math.cos(angle) * 8;
            lightData.light.position.z = Math.sin(angle) * 8;
        } else {
            // 点光源运动
            lightData.phase += lightData.speed * 0.016;
            const originalPos = lightData.originalPosition;
            lightData.light.position.x = originalPos.x + Math.sin(lightData.phase) * 2;
            lightData.light.position.y = originalPos.y + Math.cos(lightData.phase * 1.3) * 1;
            lightData.light.position.z = originalPos.z + Math.sin(lightData.phase * 0.8) * 2;
            
            // 光强度变化
            lightData.light.intensity = 0.3 + Math.sin(lightData.phase * 2) * 0.2;
        }
    });
    
    // 材质动画
    materialAnimations.forEach((matData) => {
        matData.phase += matData.speed * 0.016;
        const intensity = matData.originalEmissiveIntensity + Math.sin(matData.phase) * 0.2;
        matData.material.emissiveIntensity = Math.max(0, intensity);
    });
    
    // 闪烁效果动画
    if (sparkles) {
        const sparkleTime = Date.now() * 0.001;
        sparkles.children.forEach(sparkle => {
            const userData = sparkle.userData;
            sparkle.position.y = userData.originalY + Math.sin(sparkleTime * userData.speed) * userData.amplitude;
            sparkle.material.opacity = 0.3 + Math.sin(sparkleTime * userData.speed * 2) * 0.5;
        });
    }
    
    // 状态指示灯闪烁
    if (batterySwapStation) {
        const indicatorTime = Date.now() * 0.003;
        batterySwapStation.children.forEach(child => {
            if (child.material && child.material.emissive) {
                const intensity = 0.3 + Math.sin(indicatorTime) * 0.2;
                child.material.emissiveIntensity = intensity;
            }
        });
    }
    
    // 渲染场景（使用后处理）
    if (composer) {
        composer.render();
    } else {
        renderer.render(scene, camera);
    }
}

// 开始演示动画
function startDemo() {
    if (isAnimating) return;
    isAnimating = true;
    
    const startBtn = document.getElementById('start-demo');
    startBtn.textContent = '演示中...';
    startBtn.disabled = true;
    
    // 动画序列
    animateRobotArm()
        .then(() => animateBatteryPickup())
        .then(() => animateBatteryTransport())
        .then(() => animateBatteryInstall())
        .then(() => {
            startBtn.textContent = '开始演示';
            startBtn.disabled = false;
            isAnimating = false;
        });
}

// 开始骑手换电动画
function startRiderSwapDemo() {
    if (riderSwapAnimation) return;
    
    riderSwapAnimation = true;
    
    // 重置所有位置
    resetRiderDemo();
    riderSwapAnimation = true; // 重置后重新设置为true
    
    animateRiderSwap();
}

// 暂停演示
function pauseDemo() {
    isAnimating = false;
    riderSwapAnimation = false;
}

// 机械臂动画
function animateRobotArm() {
    return new Promise(resolve => {
        const duration = 2000;
        const startTime = Date.now();
        const startRotation = robotArm.rotation.y;
        const targetRotation = startRotation + Math.PI / 2;
        
        function animate() {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            robotArm.rotation.y = startRotation + (targetRotation - startRotation) * easeInOut(progress);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                resolve();
            }
        }
        animate();
    });
}

// 电池拾取动画
function animateBatteryPickup() {
    return new Promise(resolve => {
        const duration = 1500;
        const startTime = Date.now();
        const startY = battery.position.y;
        const targetY = 1.7;
        
        function animate() {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            battery.position.y = startY + (targetY - startY) * easeInOut(progress);
            battery.position.x = 3 - 2 * easeInOut(progress);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                resolve();
            }
        }
        animate();
    });
}

// 电池运输动画
function animateBatteryTransport() {
    return new Promise(resolve => {
        const duration = 2000;
        const startTime = Date.now();
        
        function animate() {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            robotArm.rotation.y = Math.PI / 2 - (Math.PI / 2) * easeInOut(progress);
            battery.position.x = 1 - 1 * easeInOut(progress);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                resolve();
            }
        }
        animate();
    });
}

// 电池安装动画
function animateBatteryInstall() {
    return new Promise(resolve => {
        const duration = 1500;
        const startTime = Date.now();
        const startY = battery.position.y;
        const targetY = 2.2;
        
        function animate() {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            battery.position.y = startY + (targetY - startY) * easeInOut(progress);
            battery.position.z = 0.8 * easeInOut(progress);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                resolve();
            }
        }
        animate();
    });
}

// 重置演示
function resetDemo() {
    if (isAnimating) return;
    
    // 重置机械臂位置
    robotArm.rotation.y = 0;
    
    // 重置电池位置
    battery.position.set(3, 0.2, 0);
    
    // 重置按钮状态
    const startBtn = document.getElementById('start-demo');
    startBtn.textContent = '开始演示';
    startBtn.disabled = false;
}

// 重置骑手换电演示
function resetRiderDemo() {
    riderSwapAnimation = false;
    
    // 重置骑手位置
    rider.position.set(5, 0, 2);
    rider.rotation.y = 0;
    
    // 重置电动车位置
    electricBike.position.set(4, 0, 2);
    
    // 重置旧电池位置（在电动车内）
    oldBattery.position.set(4, 0.6, 2);
    oldBattery.visible = true;
    
    // 重置新电池位置
    battery.position.set(3, 0.2, 0);
    battery.visible = true;
    
    // 重置机械臂
    robotArm.rotation.y = 0;
    robotArm.position.set(-2, 0, 0);
}

// 骑手换电动画序列
function animateRiderSwap() {
    if (!riderSwapAnimation) return;
    
    const duration = 2000; // 每个步骤2秒
    let animationStep = 0;
    
    function executeStep(step) {
        const startTime = Date.now();
        
        function animate() {
            if (!riderSwapAnimation) return;
            
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const easedProgress = easeInOut(progress);
            
            switch (step) {
                case 0: // 骑手走向换电柜
                    rider.position.x = 5 - easedProgress * 3; // 从5移动到2
                    rider.rotation.y = -easedProgress * Math.PI / 4; // 转向换电柜
                    break;
                    
                case 1: // 骑手取出旧电池
                    const liftHeight = easedProgress * 0.3;
                    oldBattery.position.y = 0.6 + liftHeight;
                    oldBattery.position.x = 4 - easedProgress * 1.5; // 移向骑手
                    break;
                    
                case 2: // 机械臂准备新电池
                    robotArm.rotation.y = easedProgress * Math.PI / 2;
                    battery.position.x = 3 - easedProgress * 1;
                    battery.position.y = 0.2 + easedProgress * 0.5;
                    break;
                    
                case 3: // 骑手放入旧电池到换电柜
                    oldBattery.position.x = 2.5 - easedProgress * 2.5; // 移向换电柜
                    oldBattery.position.y = 0.9 - easedProgress * 0.1;
                    if (progress >= 1) {
                        oldBattery.visible = false; // 隐藏旧电池
                    }
                    break;
                    
                case 4: // 骑手取新电池
                    battery.position.x = 2 + easedProgress * 1.5; // 移向骑手
                    battery.position.y = 0.7 + easedProgress * 0.2;
                    break;
                    
                case 5: // 骑手安装新电池到电动车
                    battery.position.x = 3.5 + easedProgress * 0.5; // 移向电动车
                    battery.position.y = 0.9 - easedProgress * 0.3;
                    break;
                    
                case 6: // 骑手回到电动车旁
                    rider.position.x = 2 + easedProgress * 2.5; // 回到电动车旁
                    rider.rotation.y = -Math.PI / 4 + easedProgress * Math.PI / 4; // 转向电动车
                    break;
                    
                case 7: // 完成动画
                    riderSwapAnimation = false;
                    return;
            }
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                animationStep++;
                if (animationStep <= 7) {
                    setTimeout(() => executeStep(animationStep), 500);
                }
            }
        }
        
        animate();
    }
    
    executeStep(0);
}

// 缓动函数
function easeInOut(t) {
    return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
}

// 窗口大小调整
function onWindowResize() {
    const container = document.getElementById('three-container');
    if (!container) return;
    
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
    
    // 更新后处理效果尺寸
    if (composer) {
        composer.setSize(container.clientWidth, container.clientHeight);
    }
    
    // 更新辉光效果分辨率
    if (bloomPass) {
        bloomPass.setSize(container.clientWidth, container.clientHeight);
    }
}

// 切换特效函数
function toggleEffects() {
    // 切换粒子系统
    if (particleSystem) {
        particleSystem.visible = !particleSystem.visible;
    }
    
    // 切换能量场
    if (energyField) {
        energyField.visible = !energyField.visible;
    }
    
    // 切换全息效果
    if (hologramEffect) {
        hologramEffect.visible = !hologramEffect.visible;
    }
    
    // 切换动态光照
    dynamicLights.forEach(lightData => {
        if (lightData.light.type !== 'DirectionalLight' && lightData.light.type !== 'AmbientLight') {
            lightData.light.visible = !lightData.light.visible;
        }
    });
    
    // 切换后处理效果
    if (unrealBloomPass) {
        unrealBloomPass.enabled = !unrealBloomPass.enabled;
    }
    
    console.log('3D特效已切换');
}

// 初始化事件监听器
function initEventListeners() {
    // 确保所有按钮函数在全局作用域可用
    window.startDemo = startDemo;
    window.startRiderSwapDemo = startRiderSwapDemo;
    window.pauseDemo = pauseDemo;
    window.resetDemo = resetDemo;
    window.resetRiderDemo = resetRiderDemo;
    window.toggleEffects = toggleEffects;
    
    // 演示按钮
    const startBtn = document.getElementById('start-demo');
    const resetBtn = document.getElementById('reset-demo');
    
    if (startBtn) startBtn.addEventListener('click', startDemo);
    if (resetBtn) resetBtn.addEventListener('click', resetDemo);
    
    // 导航链接
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
    
    // 联系表单
    const contactForm = document.querySelector('.contact-form form');
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('感谢您的留言！我们会尽快与您联系。');
            this.reset();
        });
    }
}

// 滚动到演示区域
function scrollToDemo() {
    const demoSection = document.getElementById('demo');
    if (demoSection) {
        demoSection.scrollIntoView({ behavior: 'smooth' });
    }
}

// 初始化滚动动画
function initScrollAnimations() {
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
    
    // 观察需要动画的元素
    document.querySelectorAll('.feature-card, .contact-content').forEach(el => {
        el.classList.add('fade-in');
        observer.observe(el);
    });
}

// 导航栏滚动效果
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    if (window.scrollY > 50) {
        navbar.style.background = 'rgba(0, 0, 0, 0.95)';
    } else {
        navbar.style.background = 'rgba(0, 0, 0, 0.9)';
    }
});