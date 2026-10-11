package com.smartstay.service;

import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.model.Branch;
import com.smartstay.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public Branch getBranchById(Integer branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));
    }

    public Branch createBranch(Branch branch) {
        branchRepository.save(branch);
        return branch;
    }

    public Branch updateBranch(Branch branch) {
        branchRepository.update(branch);
        return branch;
    }

    public void deleteBranch(Integer branchId) {
        branchRepository.delete(branchId);
    }
}
