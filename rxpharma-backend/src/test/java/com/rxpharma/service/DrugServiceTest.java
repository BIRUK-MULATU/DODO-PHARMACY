//package com.rxpharma.service;
//
//import com.rxpharma.entity.Drug;
//import com.rxpharma.entity.Supplier;
//import com.rxpharma.repository.DrugRepository;
//import com.rxpharma.repository.SupplierRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DrugServiceTest {
//
//    @Mock
//    private DrugRepository drugRepository;
//
//    @Mock
//    private SupplierRepository supplierRepository;
//
//    @InjectMocks
//    private DrugService drugService;
//
//    private Supplier supplier;
//    private Drug drug;
//
//    @BeforeEach
//    void setup() {
//
//        supplier = Supplier.builder()
//                .id(1L)
//                .companyName("ABC Pharma")
//                .contactPerson("John")
//                .email("abc@test.com")
//                .phone("0911111111")
//                .status(Supplier.Status.ACTIVE)
//                .supplierType(Supplier.SupplierType.WHOLESALER)
//                .build();
//
//        drug = Drug.builder()
//                .id(1L)
//                .name("Paracetamol")
//                .sku("PARA001")
//                .category("Tablet")
//                .price(BigDecimal.valueOf(20))
//                .stockQty(100)
//                .expiryDate(LocalDate.now().plusMonths(6))
//                .supplier(supplier)
//                .build();
//    }
//
//    @Test
//    void getDrugById_ShouldReturnDrug() {
//
//        when(drugRepository.findById(1L))
//                .thenReturn(Optional.of(drug));
//
//        Drug result = drugService.getDrugById(1L);
//
//        assertNotNull(result);
//        assertEquals("Paracetamol", result.getName());
//
//        verify(drugRepository).findById(1L);
//    }
//
//    @Test
//    void createDrug_ShouldSaveDrug() {
//
//        when(drugRepository.existsBySku("PARA001"))
//                .thenReturn(false);
//
//        when(supplierRepository.findById(1L))
//                .thenReturn(Optional.of(supplier));
//
//        when(drugRepository.save(any(Drug.class)))
//                .thenReturn(drug);
//
//        Drug saved = drugService.createDrug(
//                "Paracetamol",
//                "PARA001",
//                "Tablet",
//                BigDecimal.valueOf(20),
//                100,
//                LocalDate.now().plusMonths(6),
//                1L
//        );
//
//        assertEquals("Paracetamol", saved.getName());
//
//        verify(drugRepository).save(any(Drug.class));
//    }
//
//    @Test
//    void deleteDrug_ShouldDeleteDrug() {
//
//        when(drugRepository.existsById(1L))
//                .thenReturn(true);
//
//        drugService.deleteDrug(1L);
//
//        verify(drugRepository).deleteById(1L);
//    }
//
//}