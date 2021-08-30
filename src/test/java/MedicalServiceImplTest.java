import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MedicalServiceImplTest {
    final String PATIENT_ID = "iddqd";
    final private String ALERT = String.format("Warning, patient with id: %s, need help", PATIENT_ID);
    final private String WARNING = "Warning";

    @BeforeAll
    private static void start() { Methods.start(); }

    @BeforeEach
    void newTest() { Methods.newTest(); }

    @AfterEach
    private void endTest() { Methods.endTest(); }

    @AfterAll
    private static void end() { Methods.end(); }

    @ParameterizedTest
    @ValueSource(strings = {"33.5", "35.1", "36.8", "38.0", "39.7"})
    void checkTemperatureTest(double param) {
        final BigDecimal normalTemperature = new BigDecimal("37.0");
        final BigDecimal patientTemperature = new BigDecimal(param);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        final String expected =
                (normalTemperature
                        .subtract(new BigDecimal("1.5"))
                        .compareTo(patientTemperature) > 0)
                        ? WARNING
                        : "";
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo = mock(HealthInfo.class);
        final SendAlertService sendAlertService = mock(SendAlertServiceImpl.class);
        final PatientInfo patientInfo = mock(PatientInfo.class);
        when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        when(healthInfo.getNormalTemperature()).thenReturn(normalTemperature);
        when(patientInfoRepository.getById(PATIENT_ID)).thenReturn(patientInfo);

        final MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkTemperature(PATIENT_ID, patientTemperature);
        final String result = output.toString();
        assertTrue(result.startsWith(expected));
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 120, 150, 200})
    void checkBloodPressureTest(int param) {
        final BloodPressure patientPressure = new BloodPressure(param, 80);
        final BloodPressure bloodPressure = new BloodPressure(120, 80);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        String expected = (!patientPressure.equals(bloodPressure)) ? WARNING : "";
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo = mock(HealthInfo.class);
        final PatientInfo patientInfo = mock(PatientInfo.class);
        when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        when(patientInfo.getHealthInfo().getBloodPressure()).thenReturn(bloodPressure);
        when(patientInfoRepository.getById(PATIENT_ID))
                .thenReturn(new PatientInfo(PATIENT_ID, "John", "Doe", LocalDate.now(),
                        new HealthInfo(new BigDecimal("36.6"), patientPressure)));
        final SendAlertService sendAlertService = mock(SendAlertServiceImpl.class);
        doCallRealMethod().when(sendAlertService).send(ALERT);
//        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);


        final MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
//        Mockito.verify(sendAlertService.send(argumentCaptor.capture()));
//        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
//        System.out.println(argumentCaptor.getValue());
        medicalService.checkBloodPressure(PATIENT_ID, bloodPressure);
        final String result = output.toString();
        assertTrue(result.startsWith(expected));
    }
}
