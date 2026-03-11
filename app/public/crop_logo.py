from PIL import Image
import numpy as np

# 打开图片
img = Image.open('logo.png')

# 转换为 RGBA 模式
img = img.convert('RGBA')

# 获取 alpha 通道
data = np.array(img)
alpha = data[:, :, 3]

# 找到非透明像素的边界
non_zero = np.where(alpha > 0)
if len(non_zero[0]) > 0:
    min_y, max_y = non_zero[0].min(), non_zero[0].max()
    min_x, max_x = non_zero[1].min(), non_zero[1].max()

    # 裁剪图片
    cropped = img.crop((min_x, min_y, max_x + 1, max_y + 1))

    # 添加一点边距
    padding = 20
    new_size = (cropped.width + padding * 2, cropped.height + padding * 2)
    result = Image.new('RGBA', new_size, (0, 0, 0, 0))
    result.paste(cropped, (padding, padding))

    # 保存，覆盖原文件
    result.save('logo.png')
    print(f'Cropped from {img.size} to {result.size}')
else:
    print('No non-transparent pixels found')
