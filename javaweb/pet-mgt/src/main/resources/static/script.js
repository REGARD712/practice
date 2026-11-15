/*
 * 公共前端交互脚本：为全站提供操作确认、表单校验、图片上传预览、宠物相册切换和图片放大弹窗。
 * 各功能均通过 data 属性或既有 class 查找元素，未出现对应元素的页面会自动跳过相关初始化。
 */
// 等待 HTML 文档解析完成后再查询元素和绑定事件，避免脚本加载时目标节点尚未生成。
document.addEventListener('DOMContentLoaded', () => {
    // 查找所有声明了 data-confirm 的表单，例如删除宠物、删除用户或提交审核的表单。
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        // 在表单提交前弹出浏览器确认框，让用户确认具有破坏性或需要谨慎执行的操作。
        form.addEventListener('submit', event => {
            // 优先使用表单自定义的确认文案；未填写时使用通用提示，取消确认则阻止提交。
            if (!window.confirm(form.dataset.confirm || '确定执行此操作吗？')) {
                event.preventDefault();
            }
        });
    });

    // 遍历所有启用 Bootstrap 校验样式的表单，为它们统一接入浏览器原生约束校验。
    document.querySelectorAll('form.needs-validation').forEach(form => {
        // 每次提交时先更新自定义校验结果，再决定能否真正提交表单。
        form.addEventListener('submit', event => {
            // 一张表单可能包含确认密码框；逐个检查其与对应原密码框的值是否一致。
            form.querySelectorAll('[data-confirm-password]').forEach(input => {
                // data-confirm-password 保存原密码输入框的 CSS 选择器，据此取得用于比较的密码值。
                const password = document.querySelector(input.dataset.confirmPassword);
                // 一致时清空自定义错误，不一致时写入错误消息，使 checkValidity 返回 false。
                input.setCustomValidity(input.value === password.value ? '' : '两次输入的密码不一致');
            });
            // 检查必填、格式、长度及上面的自定义错误；校验失败时阻止提交并停止事件继续传播。
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            // 无论校验是否通过都添加 Bootstrap 状态类，让浏览器反馈和错误提示可见。
            form.classList.add('was-validated');
        });
    });

    // 图片选择框和预览容器由后台宠物表单提供；其他页面上这两个常量可能为 null。
    const imageInput = document.querySelector('[data-image-input]');
    const preview = document.querySelector('[data-image-preview]');
    // 只有两个元素同时存在时才初始化上传预览，避免在普通页面绑定无意义的事件。
    if (imageInput && preview) {
        // 用户重新选择本地图片后，重新检查文件并生成预览。
        imageInput.addEventListener('change', () => {
            // 清空上一次选择留下的缩略图，确保预览只反映当前文件集合。
            preview.innerHTML = '';
            // 将 FileList 转为数组；没有选中文件时使用空数组，便于统一遍历。
            const files = Array.from(imageInput.files || []);
            // 一次最多允许五张图片；超限时提示、清空选择并终止整个 change 处理。
            if (files.length > 5) {
                window.alert('一次最多上传 5 张图片');
                imageInput.value = '';
                return;
            }
            // 逐张检查文件大小，并通过 FileReader 生成本地预览。
            files.forEach(file => {
                // 单张图片不得超过 2MB；超限时清空选择和已有预览，并跳过当前文件的读取。
                if (file.size > 2 * 1024 * 1024) {
                    window.alert(`${file.name} 超过 2MB，请重新选择`);
                    imageInput.value = '';
                    preview.innerHTML = '';
                    return;
                }
                // FileReader 以异步方式读取本地文件，不会在上传前把图片发送到服务器。
                const reader = new FileReader();
                // 文件读取完成后，将读取结果转换为一个可展示的缩略图元素。
                reader.addEventListener('load', event => {
                    const image = document.createElement('img');
                    // load 事件的 result 是图片 Data URL，可直接作为 img 的 src。
                    image.src = event.target.result;
                    // 设置无障碍替代文本，说明这些图片尚未随表单上传。
                    image.alt = '待上传图片预览';
                    // 复用公共样式控制预览图尺寸、裁剪和边框。
                    image.className = 'upload-preview-image';
                    // 将生成的图片追加到预览区域，保留本次选择中的其他缩略图。
                    preview.appendChild(image);
                });
                // 开始以 Data URL 形式读取文件，完成后触发上面的 load 回调。
                reader.readAsDataURL(file);
            });
        });
    }

    // 宠物详情页的主图是缩略图点击后更新的目标元素。
    const mainImage = document.querySelector('[data-main-pet-image]');
    // 为相册中的每张缩略图绑定点击切换功能。
    document.querySelectorAll('[data-image-thumb]').forEach(thumb => {
        thumb.addEventListener('click', () => {
            // 页面没有主图时不执行切换，避免访问空元素属性。
            if (!mainImage) return;
            // 缩略图的 data-image-thumb 保存完整图片地址，点击后同步到主图。
            mainImage.src = thumb.dataset.imageThumb;
            // 先移除所有缩略图的选中样式，保证同一时间只有一张图片高亮。
            document.querySelectorAll('[data-image-thumb]').forEach(item => item.classList.remove('active'));
            // 再给当前点击的缩略图添加选中样式，与主图内容对应。
            thumb.classList.add('active');
        });
    });

    // 图片预览 Modal 只存在于宠物详情页，先取得其根元素。
    const imageModalElement = document.querySelector('#imagePreviewModal');
    // 弹窗与主图同时存在时才绑定放大查看功能。
    if (imageModalElement && mainImage) {
        // 找到弹窗内用于展示大图的 img 元素。
        const modalImage = imageModalElement.querySelector('img');
        // 点击主图时，将当前主图地址复制到弹窗；缩略图切换后的地址也能正确同步。
        mainImage.addEventListener('click', () => {
            modalImage.src = mainImage.src;
            // 复用已有 Bootstrap Modal 实例或按需创建实例，然后显示图片预览弹窗。
            bootstrap.Modal.getOrCreateInstance(imageModalElement).show();
        });
    }

    // 除提交时校验外，再监听确认密码框的每次输入，提供实时的一致性反馈。
    document.querySelectorAll('[data-confirm-password]').forEach(input => {
        input.addEventListener('input', () => {
            // 根据 data 属性定位对应的原密码框，取得最新输入值。
            const password = document.querySelector(input.dataset.confirmPassword);
            // 实时设置或清除自定义错误，使用户无需再次提交即可看到校验状态变化。
            input.setCustomValidity(input.value === password.value ? '' : '两次输入的密码不一致');
        });
    });
});
