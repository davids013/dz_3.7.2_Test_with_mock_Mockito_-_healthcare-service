import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MedicalServiceImplTest {
    final String PATIENT_ID = "iddqd";
    final private String ALERT = String.format("Warning, patient with id: %s, need help", PATIENT_ID);

    @BeforeAll
    private static void start() { Methods.start(); }

    @BeforeEach
    void newTest() { Methods.newTest(); }

    @AfterEach
    private void endTest() { Methods.endTest(); }

    @AfterAll
    private static void end() { Methods.end(); }

    @ParameterizedTest
    @ValueSource(doubles = {33.5, 35.1, 36.8, 38.0, 39.7})
    void checkTemperatureTest(double param) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        final BigDecimal normalTemperature = new BigDecimal(37.0);
        final BigDecimal patientTemperature = new BigDecimal(param);
        final PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo = mock(HealthInfo.class);
        final SendAlertService sendAlertService = mock(SendAlertServiceImpl.class);
        final PatientInfo patientInfo =
                new PatientInfo(PATIENT_ID, "John", "Doe", LocalDate.now(), healthInfo);
        when(healthInfo.getNormalTemperature()).thenReturn(normalTemperature);
        when(patientInfoRepository.getById(PATIENT_ID)).thenReturn(patientInfo);
        final String expected =
                (normalTemperature
                        .subtract(new BigDecimal("1.5"))
                        .compareTo(patientTemperature) > 0)
                        ? ALERT
                        : "";
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkTemperature(PATIENT_ID, patientTemperature);
        assertEquals(expected, output.toString());
    }

    @Test
    void checkBloodPressureTest() {
        final BloodPressure patientPressure = new BloodPressure(180, 90);
        final BloodPressure bloodPressure = new BloodPressure(180, 90);
        //String alert = "Achtung!";
        final PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo =
                mock(HealthInfo.class);
//                new HealthInfo(new BigDecimal(36.6), new BloodPressure(120, 70));
        final PatientInfo patientInfo = mock(PatientInfo.class);
//                new PatientInfo(
//                PATIENT_ID, "John", "Doe", LocalDate.now(), healthInfo);
        when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        when(patientInfo.getHealthInfo().getBloodPressure()).thenReturn(bloodPressure);
        //when(healthInfo.getBloodPressure()).thenReturn(bloodPressure);
//        when(patientInfoRepository.getById(PATIENT_ID)).thenReturn(patientInfo);
        when(patientInfoRepository.getById(PATIENT_ID))
                .thenReturn(new PatientInfo(PATIENT_ID, "John", "Doe", LocalDate.now(),
                        new HealthInfo(new BigDecimal(36.6), patientPressure)));
        SendAlertService sendAlertService =
//                new SendAlertServiceImpl();
                mock(SendAlertServiceImpl.class);
        doCallRealMethod().when(sendAlertService).send(ALERT);
//        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);


        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
//        Mockito.verify(sendAlertService.send(argumentCaptor.capture()));
//        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
//        System.out.println(argumentCaptor.getValue());
        medicalService.checkBloodPressure(PATIENT_ID, bloodPressure);
    }

    @Test
    void getPatientInfoTest() {
        PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        when(patientInfoRepository.getById(PATIENT_ID))
                .thenReturn(new PatientInfo(
                        PATIENT_ID, "John", "Doe", LocalDate.now(), new HealthInfo()));
    }
}
