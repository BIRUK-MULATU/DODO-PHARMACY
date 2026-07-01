//package com.rxpharma.service;
//
//import com.rxpharma.dto.request.SupplierRequest;
//import com.rxpharma.entity.Supplier;
//import com.rxpharma.repository.SupplierRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SupplierServiceTest {
//
//    @Mock
//    private SupplierRepository supplierRepository;
//
//    @InjectMocks
//    private SupplierService supplierService;
//
//    private Supplier supplier;
//    private SupplierRequest request;
//
//    @BeforeEach
//    void setUp() {
//
//        supplier = Supplier.builder()
//                .id(1L)
//                .companyName("ABC Pharma")
//                .contactPerson("John")
//                .email("abc@test.com")
//                .phone("0911111111")
//                .status(Supplier.Status.ACTIVE)
//                .supplierType(Supplier.SupplierType.WHOLESALER)
//                .address("Addis Ababa")
//                .build();
//
//        request = new SupplierRequest();
//        request.setCompanyName("ABC Pharma");
//        request.setContactPerson("John");
//        request.setEmail("abc@test.com");
//        request.setPhone("0911111111");
//        request.setStatus(Supplier.Status.ACTIVE);
//        request.setSupplierType(Supplier.SupplierType.WHOLESALER);
//        request.setAddress("Addis Ababa");
//    }
//
//    @Test
//    void getAllSuppliers_ShouldReturnList() {
//
//        when(supplierRepository.findAll())
//                .thenReturn(List.of(supplier));
//
//        List<Supplier> result = supplierService.getAllSuppliers();
//
//        assertEquals(1, result.size());
//        assertEquals("ABC Pharma", result.get(0).getCompanyName());
//
//        verify(supplierRepository).findAll();
//    }
//
//    @Test
//    void getSupplierById_ShouldReturnSupplier() {
//
//        when(supplierRepository.findById(1L))
//                .thenReturn(Optional.of(supplier));
//
//        Supplier result = supplierService.getSupplierById(1L);
//
//        assertNotNull(result);
//        assertEquals("ABC Pharma", result.getCompanyName());
//
//        verify(supplierRepository).findById(1L);
//    }
//
//    @Test
//    void getSupplierById_ShouldThrowException_WhenNotFound() {
//
//        when(supplierRepository.findById(1L))
//                .thenReturn(Optional.empty());
//
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> supplierService.getSupplierById(1L));
//
//        assertTrue(ex.getMessage().contains("Supplier not found"));
//    }
//
//    @Test
//    void getSuppliersByType_ShouldReturnSuppliers() {
//
//        when(supplierRepository.findBySupplierType(Supplier.SupplierType.WHOLESALER))
//                .thenReturn(List.of(supplier));
//
//        List<Supplier> result =
//                supplierService.getSuppliersByType(Supplier.SupplierType.WHOLESALER);
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void createSupplier_ShouldSaveSupplier() {
//
//        when(supplierRepository.existsByEmail(request.getEmail()))
//                .thenReturn(false);
//
//        when(supplierRepository.save(any(Supplier.class)))
//                .thenReturn(supplier);
//
//        Supplier result = supplierService.createSupplier(request);
//
//        assertEquals("ABC Pharma", result.getCompanyName());
//
//        verify(supplierRepository).save(any(Supplier.class));
//    }
//
//    @Test
//    void createSupplier_ShouldThrowException_WhenEmailExists() {
//
//        when(supplierRepository.existsByEmail(request.getEmail()))
//                .thenReturn(true);
//
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> supplierService.createSupplier(request));
//
//        assertTrue(ex.getMessage().contains("Email already in use"));
//    }
//
//    @Test
//    void updateSupplier_ShouldUpdateSupplier() {
//
//        when(supplierRepository.findById(1L))
//                .thenReturn(Optional.of(supplier));
//
//        when(supplierRepository.save(any(Supplier.class)))
//                .thenReturn(supplier);
//
//        Supplier updated = supplierService.updateSupplier(1L, request);
//
//        assertEquals("ABC Pharma", updated.getCompanyName());
//
//        verify(supplierRepository).save(any(Supplier.class));
//    }
//
//    @Test
//    void deleteSupplier_ShouldDeleteSupplier() {
//
//        when(supplierRepository.existsById(1L))
//                .thenReturn(true);
//
//        supplierService.deleteSupplier(1L);
//
//        verify(supplierRepository).deleteById(1L);
//    }
//
//    @Test
//    void deleteSupplier_ShouldThrowException_WhenSupplierNotFound() {
//
//        when(supplierRepository.existsById(1L))
//                .thenReturn(false);
//
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> supplierService.deleteSupplier(1L));
//
//        assertTrue(ex.getMessage().contains("Supplier not found"));
//    }
//}