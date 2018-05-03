package com.cros.block.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: ValidCodeUtil
 * @Package com.cros.util
 * @Description:TODO ADD FUNCTION
 * @date: 2016年12月4日 上午12:42:58
 * @author hokuny@foxmail.com
 * @version 
 */
public class ValidCodeUtil {
	
	/**
	 * @Title: generate
	 * @Description: TODO(生成验证码，并把验证码存储到session)
	 * @param key
	 * @param request
	 * @param response
	 * @throws Exception
	 * @return void
	 */
	public static void generate(String key,HttpServletRequest request,HttpServletResponse response) throws Exception{
		int width = 60, height = 20;
		BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Random random = new Random();
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 155; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}
		String validCode = "";
		for (int i = 0; i < 4; i++) {
			String rand = String.valueOf(random.nextInt(10));
			validCode += rand;
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
			g.drawString(rand, 13 * i + 6, 16);
		}
		request.getSession().setAttribute(key, validCode);
		g.dispose();
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		ServletOutputStream responseOutputStream;
		responseOutputStream = response.getOutputStream();
		ImageIO.write(image, "JPEG", response.getOutputStream());
		
		responseOutputStream.flush();
		responseOutputStream.close();
	}	
	
	private static Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}
