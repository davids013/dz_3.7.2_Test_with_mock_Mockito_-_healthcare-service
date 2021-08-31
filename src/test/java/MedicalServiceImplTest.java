import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MedicalServiceImplTest {
    final String PATIENT_ID = "iddqd";

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
        final String WARNING = "Warning";
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
        final PatientInfoRepository patientInfoRepository =
                mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo = mock(HealthInfo.class);
        final SendAlertService sendAlertService = mock(SendAlertServiceImpl.class);
        final PatientInfo patientInfo = mock(PatientInfo.class);
        when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        when(healthInfo.getNormalTemperature()).thenReturn(normalTemperature);
        when(patientInfoRepository.getById(PATIENT_ID)).thenReturn(patientInfo);
        final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        final MedicalService medicalService =
                new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkTemperature(PATIENT_ID, patientTemperature);
        verify(healthInfo, times(1)).getNormalTemperature();
        verify(patientInfoRepository, only()).getById(argumentCaptor.capture());
        final String result = output.toString();
        assertEquals(PATIENT_ID, argumentCaptor.getValue());
        assertTrue(result.startsWith(expected));
    }

    @ParameterizedTest
    @MethodSource("getIntegers")
    void checkBloodPressureTest(int param) {
        final BloodPressure patientPressure = new BloodPressure(param, 80);
        final BloodPressure bloodPressure = new BloodPressure(120, 80);
        final PatientInfoRepository patientInfoRepository =
                mock(PatientInfoFileRepository.class);
        final HealthInfo healthInfo = mock(HealthInfo.class);
        final PatientInfo patientInfo = mock(PatientInfo.class);
        final SendAlertService sendAlertService = mock(SendAlertServiceImpl.class);
        when(patientInfoRepository.getById(any())).thenReturn(patientInfo);
        when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        when(healthInfo.getBloodPressure()).thenReturn(patientPressure);
        doCallRealMethod().when(sendAlertService).send(any());
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        final MedicalService medicalService =
                new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkBloodPressure(PATIENT_ID, bloodPressure);
        verify(patientInfoRepository).getById(captor.capture());
        assertEquals(PATIENT_ID, captor.getValue());
        verify(sendAlertService,
                times((patientPressure.equals(bloodPressure)) ? 0 : 1)).send(any());
    }

    private static Stream<Integer> getIntegers() {
        return Stream
                .iterate(80, n -> n + 20)
                .limit(5);
    }
}
