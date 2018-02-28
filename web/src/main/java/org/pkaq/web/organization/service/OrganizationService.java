package org.pkaq.web.organization.service;

import org.pkaq.web.organization.entity.OrganizationEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizationService {
    public List<OrganizationEntity> listOrg(){
        return null;
    }

    public boolean deleteOrg(){
        return false;
    }

    public boolean updateOrg(){
        return false;
    }

    public boolean insertOrg(){
        return false;
    }
}
