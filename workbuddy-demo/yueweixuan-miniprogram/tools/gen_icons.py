# -*- coding: utf-8 -*-
"""
悦味轩点单小程序 - 图标资源生成脚本
统一线性图标风格：圆角线条 + 统一主色 (#E8552F)
生成内容：tabBar 图标(常态灰色/选中主色)、空状态图、菜品占位图
用法：python gen_icons.py
"""
import os
from PIL import Image, ImageDraw

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "miniprogram", "assets")
ICONS = os.path.join(BASE, "icons")
IMAGES = os.path.join(BASE, "images")

PRIMARY = (232, 85, 47, 255)      # 主色 暖橙红
GRAY = (166, 166, 166, 255)       # 未选中灰
LINE_W = 11                        # 线宽(在4x画布上)
SS = 4                             # 超采样倍数
SIZE = 81                          # tabBar 图标标准尺寸(px)


def new_canvas(size=324):
    """新建 4x 透明画布"""
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def rounded_line(d, pts, width=LINE_W, fill=PRIMARY):
    d.line(pts, fill=fill, width=width, joint="curve")
    r = width // 2
    for (x, y) in (pts[0], pts[-1]):
        d.ellipse([x - r, y - r, x + r, y + r], fill=fill)


def draw_home(color):
    """首页：小房子（屋顶 + 门体）"""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # 屋顶：三角形两条边
    rounded_line(d, [(52, 172), (162, 62), (272, 172)], fill=color)
    # 房体：两侧竖线 + 底边
    rounded_line(d, [(92, 152), (92, 262), (232, 262), (232, 152)], fill=color)
    # 门
    rounded_line(d, [(142, 262), (142, 200), (182, 200), (182, 262)], fill=color, width=9)
    return img


def draw_menu(color):
    """点单分类：2x2 圆角方块网格"""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    s, gap, r = 108, 24, 26
    xs = [60, 60 + s + gap]
    ys = [60, 60 + s + gap]
    for x in xs:
        for y in ys:
            d.rounded_rectangle([x, y, x + s, y + s], radius=r, outline=color, width=LINE_W)
    return img


def draw_cart(color):
    """购物车：车筐 + 双轮 + 拉手"""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # 拉手 + 筐口左沿
    rounded_line(d, [(54, 82), (92, 82), (118, 214)], fill=color)
    # 筐口右沿
    rounded_line(d, [(268, 108), (292, 108), (252, 214), (126, 214)], fill=color)
    # 筐底封闭
    rounded_line(d, [(126, 214), (252, 214)], fill=color)
    # 双轮
    for cx in (150, 236):
        d.ellipse([cx - 26, 244, cx + 26, 296], outline=color, width=LINE_W)
    return img


def draw_order(color):
    """订单记录：小票（圆角矩形 + 三条内容线）"""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([84, 52, 240, 272], radius=30, outline=color, width=LINE_W)
    for y in (120, 172, 224):
        w = 108 if y == 224 else 128
        rounded_line(d, [(118, y), (118 + w, y)], fill=color, width=9)
    return img


def save_icon(img, name):
    img = img.resize((SIZE, SIZE), Image.LANCZOS)
    os.makedirs(ICONS, exist_ok=True)
    img.save(os.path.join(ICONS, name))
    print("icon:", name)


def gen_tabbar():
    tabs = {"home": draw_home, "menu": draw_menu, "cart": draw_cart, "order": draw_order}
    for name, fn in tabs.items():
        save_icon(fn(GRAY), f"{name}.png")
        save_icon(fn(PRIMARY), f"{name}-active.png")


def gen_empty():
    """空状态：餐盘 + 叉勺剪影，浅灰暖色"""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    soft = (196, 190, 184, 255)
    # 外盘
    d.ellipse([32, 92, 292, 292], outline=soft, width=12)
    # 内盘
    d.ellipse([84, 144, 240, 240], outline=soft, width=10)
    # 叉(左) 勺(右)
    rounded_line(d, [(120, 60), (120, 130)], fill=soft, width=10)
    d.line([(108, 70), (108, 110)], fill=soft, width=8)
    d.line([(132, 70), (132, 110)], fill=soft, width=8)
    rounded_line(d, [(204, 60), (204, 130)], fill=soft, width=10)
    d.ellipse([190, 46, 218, 92], outline=soft, width=8)
    img = img.resize((200, 200), Image.LANCZOS)
    os.makedirs(IMAGES, exist_ok=True)
    img.save(os.path.join(IMAGES, "empty.png"))
    print("image: empty.png")


def gen_dish_placeholders():
    """菜品占位图：暖色渐变 + 餐盘剪影（正式环境替换为云存储图片）"""
    palettes = [
        ((253, 235, 220), (240, 178, 140)),
        ((253, 228, 214), (238, 158, 122)),
        ((252, 239, 219), (245, 190, 120)),
        ((250, 230, 226), (235, 150, 132)),
        ((253, 232, 205), (242, 172, 106)),
        ((250, 226, 214), (236, 145, 118)),
    ]
    os.makedirs(IMAGES, exist_ok=True)
    for i, (c1, c2) in enumerate(palettes, 1):
        w, h = 480, 360
        img = Image.new("RGB", (w, h), c1)
        d = ImageDraw.Draw(img)
        # 纵向渐变
        for y in range(h):
            t = y / h
            rgb = tuple(int(c1[k] + (c2[k] - c1[k]) * t) for k in range(3))
            d.line([(0, y), (w, y)], fill=rgb)
        # 餐盘剪影
        plate = (255, 255, 255)
        d.ellipse([110, 110, 370, 270], fill=plate)
        d.ellipse([165, 152, 315, 228], outline=(c2[0], c2[1], c2[2]), width=8)
        d.ellipse([230, 165, 250, 215], fill=(c2[0], c2[1], c2[2]))
        img.save(os.path.join(IMAGES, f"dish-{i}.png"))
        print(f"image: dish-{i}.png")


if __name__ == "__main__":
    gen_tabbar()
    gen_empty()
    gen_dish_placeholders()
    print("done")
