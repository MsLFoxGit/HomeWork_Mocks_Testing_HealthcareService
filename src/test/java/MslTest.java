import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;

public class MslTest {
    @ParameterizedTest
    @DisplayName("MslTest: checkBloodPressureTest()")
    @CsvSource({
         // id     normalHighPressure normalLowPressure pressureJumps
            "a93a,        120,               80,                1 ",
            "b33f,        120,               80,                10",
            "c93a,        120,               80,               -1 ",
            "d33f,        120,               80,               -10",
    })
    public void checkBloodPressureTest(String id,
                                       int normalHighPressure, int normalLowPressure,
                                       int pressureJump ){

        HealthInfo healthInfo = new HealthInfo(null, new BloodPressure(normalHighPressure, normalLowPressure));

        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        Mockito.when(patientInfoRepository.getById(anyString()))
                .thenReturn(new PatientInfo(id, null, null, null, healthInfo));

        SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);

        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);

        int times =0;
        // both Pressures are normal
        medicalService.checkBloodPressure(id,
                new BloodPressure(normalHighPressure, normalLowPressure));
        Mockito.verify(sendAlertService, Mockito.times(times)).send(anyString());
        // highPressure is abnormal, lowPressure is normal
        medicalService.checkBloodPressure(id,
                new BloodPressure(normalHighPressure + pressureJump, normalLowPressure));
        Mockito.verify(sendAlertService, Mockito.times(++times)).send(anyString());
        // highPressure is normal, lowPressure is abnormal
        medicalService.checkBloodPressure(id,
                new BloodPressure(normalHighPressure, normalLowPressure + pressureJump));
        Mockito.verify(sendAlertService, Mockito.times(++times)).send(anyString());
        // both Pressures are abnormal
        medicalService.checkBloodPressure(id,
                new BloodPressure(normalHighPressure + pressureJump, normalLowPressure + pressureJump));
        Mockito.verify(sendAlertService, Mockito.times(++times)).send(anyString());

        // Capturing message from sendAlertService.send(message)
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService,Mockito.times(times)).send(argCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: " + id + ", need help", argCaptor.getValue());
    }

    @ParameterizedTest
    @DisplayName("MslTest: checkTemperatureTest()")
    @CsvSource({
            // id     normaTemperature temperatureJumps
            "a93a,        36.6,             1.51",
            "c93a,        36.7,             2",  } )

    public void checkTemperatureTest(String id, BigDecimal normalTemperature, double temperatureJumps) {

        HealthInfo healthInfo = new HealthInfo(normalTemperature, new BloodPressure());

        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        Mockito.when(patientInfoRepository.getById(anyString()))
                .thenReturn(new PatientInfo(id, null, null, null, healthInfo));

        SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);

        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);

        int times = 0;
        // temperature is normal
        medicalService.checkTemperature(id,normalTemperature);
        Mockito.verify(sendAlertService, Mockito.times(times)).send(anyString());
        // temperature is lower by temperatureJumps
        medicalService.checkTemperature(id, normalTemperature.subtract(BigDecimal.valueOf(temperatureJumps)));
        Mockito.verify(sendAlertService, Mockito.times(++times)).send(anyString());
        // Capturing message from sendAlertService.send(message)
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService,Mockito.times(times)).send(argCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: " + id + ", need help", argCaptor.getValue());

    }
}
