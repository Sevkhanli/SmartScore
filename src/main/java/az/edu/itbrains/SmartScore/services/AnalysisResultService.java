package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.analysisResult.AnalysisResultDto;
import az.edu.itbrains.SmartScore.models.User;

public interface AnalysisResultService {
    AnalysisResultDto calculateScore(User user);
}
