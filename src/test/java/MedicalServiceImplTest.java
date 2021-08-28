import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
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

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public class MedicalServiceImplTest {

    @BeforeAll
    private static void start() { Methods.start(); }

    @BeforeEach
    void newTest() { Methods.newTest(); }

    @AfterEach
    private void endTest() { Methods.endTest(); }

    @AfterAll
    private static void end() { Methods.end(); }

    @Test
    void checkBloodPressureTest() {
        String patientId = "42672";
        BloodPressure bloodPressure = new BloodPressure(180, 90);
        String alert = "Achtung!";
        PatientInfoRepository patientInfoRepository = mock(PatientInfoFileRepository.class);
        when(patientInfoRepository.getById(patientId))
                .thenReturn(new PatientInfo(
                        patientId, "John", "Doe", LocalDate.now(), new HealthInfo()));
        SendAlertService sendAlertService = spy(SendAlertServiceImpl.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);


        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
//        Mockito.verify(sendAlertService.send(argumentCaptor.capture()));
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        System.out.println(argumentCaptor.getValue());
    }
}
