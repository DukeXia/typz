package com.izhbg.typz.shop.goods.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.POST;

import org.hibernate.annotations.common.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.izhbg.typz.base.mapper.BeanMapper;
import com.izhbg.typz.base.page.Page;
import com.izhbg.typz.base.util.Ajax;
import com.izhbg.typz.base.util.Constants;
import com.izhbg.typz.base.util.IdGenerator;
import com.izhbg.typz.shop.goods.dto.TShGoods;
import com.izhbg.typz.shop.goods.dto.TShGoodsBasic;
import com.izhbg.typz.shop.goods.dto.TShGoodsDetail;
import com.izhbg.typz.shop.goods.dto.TShGoodsImage;
import com.izhbg.typz.shop.goods.dto.TShGoodsTags;
import com.izhbg.typz.shop.goods.dto.TShGoodsType;
import com.izhbg.typz.shop.goods.service.TShGoodsBasicService;
import com.izhbg.typz.shop.goods.service.TShGoodsDetailService;
import com.izhbg.typz.shop.goods.service.TShGoodsImageService;
import com.izhbg.typz.shop.goods.service.TShGoodsTagService;
import com.izhbg.typz.shop.goods.service.TShGoodsTagsService;
import com.izhbg.typz.shop.goods.service.TShGoodsTypeService;
import com.izhbg.typz.sso.util.SpringSecurityUtils;
import com.mysql.jdbc.Messages;
/**
 * 
* @ClassName: GoodsController 
* @Description: 产品 管理相关
* @author caixl 
* @date 2016-6-27 上午10:46:14  
*
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	private TShGoodsBasicService tShGoodsBasicService;
	
	@Autowired
	private TShGoodsTypeService tShGoodsTypeService;
	
	@Autowired
	private TShGoodsDetailService tShGoodsDetailService;
	
	@Autowired
	private TShGoodsImageService tShGoodsImageService;
	@Autowired
	private TShGoodsTagsService tShGoodsTagsService;
	@Autowired
	private TShGoodsTagService tShGoodsTagService;
	
	private BeanMapper beanMapper = new BeanMapper();
	
	/**
	 * 产品列表
	 * @param page
	 * @param tShGoodsBasic
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_list")
	public String list(@ModelAttribute  Page page,
	           		@ModelAttribute TShGoods tShGoods, Model model) throws Exception {
		
		Page page_ = tShGoodsBasicService.pageList(page,tShGoods);
		List<TShGoodsTags> tags =  tShGoodsTagsService.getAll();
		model.addAttribute("page", page_);
		model.addAttribute("tShGoods", tShGoods);
		model.addAttribute("tags", tags);
		
		return "shop/goods/goods_list";
	}
	/**
	 * 店铺产品列表
	 * @param page
	 * @param tShGoods
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_store_list")
	public String StoreList(@ModelAttribute  Page page,
			@ModelAttribute TShGoods tShGoods, Model model) throws Exception {
		
		Page page_ = tShGoodsBasicService.pageStoreList(page,tShGoods);
		model.addAttribute("page", page_);
		model.addAttribute("tShGoods", tShGoods);
		
		return "shop/goods/goods_store_list";
	}
	
	/**
	 * 产品编辑
	 * @param tShGoodsBasic
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_edit")
	public String edit(@ModelAttribute TShGoodsBasic tShGoodsBasic,Model model) throws Exception{
		
		if(tShGoodsBasic!=null&&StringHelper.isNotEmpty(tShGoodsBasic.getId())){
			String randomId = tShGoodsBasic.getRandom();
			tShGoodsBasic = tShGoodsBasicService.getById(tShGoodsBasic.getId());
			tShGoodsBasic.setRandom(randomId);
			TShGoodsDetail tShGoodsDetail = tShGoodsDetailService.queryByGoodsIdAndVersion(tShGoodsBasic.getId(), -1);
			if(tShGoodsDetail!=null)
				tShGoodsBasic.settShGoodsDetail(tShGoodsDetail);
			List<TShGoodsImage> tShGoodsImages = tShGoodsImageService.queryByGoodsIdAndVersion(tShGoodsBasic.getId(), -1);
			if(tShGoodsImages!=null)
				tShGoodsBasic.settShGoodsImages(tShGoodsImages);
			//商品类目
			TShGoodsType tShGoodsType_thir = tShGoodsTypeService.getById(tShGoodsBasic.getTypeId());
			TShGoodsType tShGoodsType_sec = tShGoodsTypeService.getById(tShGoodsType_thir.getPid().split(",")[0]);
			TShGoodsType tShGoodsType_fir = tShGoodsTypeService.getById(tShGoodsType_sec.getPid().split(",")[0]);
			
			model.addAttribute("tShGoodsType_thir", tShGoodsType_thir);
			model.addAttribute("tShGoodsType_sec", tShGoodsType_sec);
			model.addAttribute("tShGoodsType_fir", tShGoodsType_fir);
			model.addAttribute("tShGoodsBasic", tShGoodsBasic);
		}
		
		List<TShGoodsType> tShGoodsTypes = tShGoodsTypeService.getListByPid(null);
		model.addAttribute("tShGoodsTypes",tShGoodsTypes);
		
		return "shop/goods/goods_edit";
	}
	/**
	 * 删除产品
	 * @param checkdel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_delete")
	@ResponseBody
	@POST
	public String delete(String[] checkdel) throws Exception{
		String result="";
		if(checkdel == null || checkdel.length < 1){
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		}
		tShGoodsBasicService.deleteBatche(checkdel);
		result = "sucess";
		return result;
	}
	/**
	 * 产品恢复
	 * @param checkdel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_recover")
	@ResponseBody
	public String recover(String[] checkdel) throws Exception{
		String result="";
		if(checkdel == null || checkdel.length < 1){
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		}
		tShGoodsBasicService.recoverBatche(checkdel);
		result = "sucess";
		return result;
	}
	/**
	 * 产品批量下架
	 * @param checkdel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_batchUnderGoods")
	@ResponseBody
	public String batchUnderGoods(String[] checkdel) throws Exception{
		String result="";
		if(checkdel == null || checkdel.length < 1){
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		}
		tShGoodsBasicService.batchUnderGoods(checkdel);
		result = "sucess";
		return result;
	}
	/**
	 * 产品批量上架
	 * @param checkdel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_batchUpGoods")
	@ResponseBody
	public String batchUpGoods(String[] checkdel) throws Exception{
		String result="";
		if(checkdel == null || checkdel.length < 1){
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		}
		tShGoodsBasicService.batchUpGoods(checkdel);
		result = "sucess";
		return result;
	}
	/**
	 * 产品保存
	 * @param tShGoods
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("goods_save")
	public String save(@ModelAttribute TShGoods tShGoods) throws Exception{
		tShGoodsBasicService.addOrUpdateGoods(tShGoods);
		if(StringHelper.isNotEmpty(tShGoods.getRandom())&&"1860010".equals(tShGoods.getRandom()))
			return "redirect:/goods/goods_list.izhbg?status=-1";
		else
			return "redirect:/goods/goods_store_list.izhbg?status=-1";
	}
	/**
	 * 产品标签
	 * @param tagId
	 * @param checkdel
	 * @param xh
	 * @return
	 */
	@RequestMapping(value = "goods-setTag", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String setTag(@RequestParam(value = "tagId", required = true, defaultValue = "") String tagId,
									   @RequestParam(value = "checkdel", required = true, defaultValue = "") String[] checkdel,
									   @RequestParam(value = "xh", required = true, defaultValue = "0") Integer xh) {
		String result = null;
		if(StringHelper.isEmpty(tagId)||checkdel==null)
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		try {
			tShGoodsTagService.setTag(tagId, checkdel, xh);
			result = Ajax.JSONResult(Constants.RESULT_CODE_SUCCESS, "设置成功");	
		} catch (Exception e) {
			result = Ajax.JSONResult(Constants.RESULT_CODE_FAILED, Messages.getString("systemMsg.fieldEmpty"));	
		}
		return result;
	}
	/**
	 * 产品标签
	 * @param tagId
	 * @param checkdel
	 * @param xh
	 * @return
	 */
	@RequestMapping(value = "goods-delTag", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String delTag(@RequestParam(value = "tagId", required = true) String tagId,
									   @RequestParam(value = "goodsId", required = true) String goodsId) {
		String result = null;
		if(StringHelper.isEmpty(tagId))
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		try {
			tShGoodsTagService.delTag(tagId, goodsId);
			result = Ajax.JSONResult(Constants.RESULT_CODE_SUCCESS, "设置成功");	
		} catch (Exception e) {
			result = Ajax.JSONResult(Constants.RESULT_CODE_FAILED, Messages.getString("systemMsg.fieldEmpty"));	
		}
		return result;
	}
	/**
	 * 设置产品销售价格
	 * @param goodsId
	 * @param price
	 * @return
	 */
	@RequestMapping(value = "goods-setSalePrice", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String setSalePrice(@RequestParam(value = "pk", required = true) String goodsId,
									   @RequestParam(value = "value", required = true) double price) {
		String result = null;
		if(StringHelper.isEmpty(goodsId)||goodsId==null)
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		try {
			tShGoodsBasicService.setSalePrice(goodsId, price);
			result = Ajax.JSONResult(Constants.RESULT_CODE_SUCCESS, "设置成功");	
		} catch (Exception e) {
			result = Ajax.JSONResult(Constants.RESULT_CODE_FAILED, Messages.getString("systemMsg.fieldEmpty"));	
		}
		return result;
	} 
	
	@RequestMapping(value = "goods-setCostPrice", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String setCostPrice(@RequestParam(value = "pk", required = true) String goodsId,
									   @RequestParam(value = "value", required = true) double price) {
		String result = null;
		if(StringHelper.isEmpty(goodsId)||goodsId==null)
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		try {
			tShGoodsBasicService.setCostPrice(goodsId, price);
			result = Ajax.JSONResult(Constants.RESULT_CODE_SUCCESS, "设置成功");	
		} catch (Exception e) {
			result = Ajax.JSONResult(Constants.RESULT_CODE_FAILED, Messages.getString("systemMsg.fieldEmpty"));	
		}
		return result;
	} 
	
	@RequestMapping(value = "goods-setPercent", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String setPercent(@RequestParam(value = "pk", required = true) String goodsId,
									   @RequestParam(value = "value", required = true) String percent) {
		String result = null;
		if(StringHelper.isEmpty(goodsId)||goodsId==null)
			result = Ajax.JSONResult(Constants.RESULT_CODE_ERROR, Messages.getString("systemMsg.fieldEmpty"));	
		try {
			tShGoodsBasicService.setPercent(goodsId, percent);
			result = Ajax.JSONResult(Constants.RESULT_CODE_SUCCESS, "设置成功");	
		} catch (Exception e) {
			result = Ajax.JSONResult(Constants.RESULT_CODE_FAILED, Messages.getString("systemMsg.fieldEmpty"));	
		}
		return result;
	} 
	@Resource
	public void settShGoodsBasicService(TShGoodsBasicService tShGoodsBasicService) {
		this.tShGoodsBasicService = tShGoodsBasicService;
	}
	
	
}