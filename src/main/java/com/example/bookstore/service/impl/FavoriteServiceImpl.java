package com.example.bookstore.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.bookstore.dao.FavoriteDao;
import com.example.bookstore.dao.ProductDao;
import com.example.bookstore.dao.UserDao;
import com.example.bookstore.entity.Favorite;
import com.example.bookstore.entity.Product;
import com.example.bookstore.entity.User;
import com.example.bookstore.model.BestSellerModel;
import com.example.bookstore.service.FavoriteService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {
	@Autowired
	ProductDao productDao;

	@Autowired
	FavoriteDao favoriteDao;

	@Autowired
	UserDao userDao;

	// @SuppressWarnings("null")
	@Override
	public Favorite create(int id) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();

		User temp = userDao.findUserByEmail(username);

		Favorite favorite = new Favorite();

		if (temp != null) {

			favorite = favoriteDao.getOneFavorite(username, id);

			if (favorite == null) {
				favorite = new Favorite();
				Date date = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String strDate = formatter.format(date);

				Product product = productDao.findById(id).get();
				favorite.setProduct(product);
				favorite.setUser(temp);
				favorite.setDate(strDate);
				favoriteDao.save(favorite);
			}

			else {
				favoriteDao.delete(favorite);
				favorite.setId(0);
			}

		}

		return favorite;
	}

	@Override
	public List<Favorite> getListFavoriteByEmail() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();

		return favoriteDao.getListFavoriteByEmail(username);
	}

	@Override
	public void delete(int id) {
		Favorite favorite = favoriteDao.findById(id).get();
		favoriteDao.delete(favorite);
	}

	@Override
	public Favorite getOneFavorite(int id) {
		Favorite favorite = new Favorite();

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = "";
		try {
			username = ((UserDetails) principal).getUsername();
		} catch (Exception e) {
		}
		User temp = userDao.findUserByEmail(username);

		if (temp != null) {
			favorite = favoriteDao.getOneFavorite(username, id);
		} else {
			favorite = null;
		}
		return favorite;
	}

	@Override
	public List<BestSellerModel> getListBestSellerProduct(Pageable topFour) {
		return favoriteDao.getListBestSellerProduct(topFour);
	}

}
