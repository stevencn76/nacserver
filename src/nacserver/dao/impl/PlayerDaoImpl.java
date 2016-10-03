package nacserver.dao.impl;

import java.util.List;

import nacserver.dao.PlayerDao;
import nacserver.dto.PageInfo;
import nacserver.entity.Player;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

@Repository("playerDao")
public class PlayerDaoImpl extends BaseDaoHibernate<Player> implements PlayerDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Player> findAll(PageInfo info) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Player.class);

		criteria.addOrder(Order.asc("name"));
		
		if(info != null){
			//分页处理
			criteria.setProjection(Projections.rowCount());
			int total = ((Long)this.getHibernateTemplate().findByCriteria(criteria).get(0)).intValue();
			info.setTotal(total);
			criteria.setProjection(null);
			criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);

			if(info.getPageSize() > 0) {
				int first = (info.getPageNumber() - 1) * info.getPageSize();
				return getHibernateTemplate().findByCriteria(criteria, first, info.getPageSize());
			}
		}
		return getHibernateTemplate().findByCriteria(criteria);
	}

}
