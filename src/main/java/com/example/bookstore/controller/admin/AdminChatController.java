package com.example.bookstore.controller.admin;

import com.example.bookstore.common.Constants;
import com.example.bookstore.dao.ChatDao;
import com.example.bookstore.entity.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminChatController {

    @Autowired
    ChatDao chatDao;


    @GetMapping("/admin/chat/list")
    public String list(Model model) {
        model.addAttribute("chats", chatDao.findAll());
        return "admin/chat/list";
    }

    @GetMapping("/admin/chat/form")
    public String form(Model model) {
        return "admin/chat/form";

    }

    @PostMapping("/admin/chat/add")
    public String add(@ModelAttribute("name") String name,
                      @ModelAttribute("intent_name") String intentName,
                      @ModelAttribute("training_phrases") String trainingPhrases,
                      @ModelAttribute("responses") String responses) {
        Chat chat = new Chat();
        chat.setName(name);
        chat.setIntentName(intentName);
        chat.setTrainingPhrases(trainingPhrases);
        chat.setResponses(responses);
        chat.setDescription(name);

        String[] trainingPhrasesArray = trainingPhrases.split("\n");
        String[] responsesArray = responses.split("\n");

        String apiUrl = "http://localhost:5000/add_intent";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String jsonBody = String.format("{\"intent_name\": \"%s\", \"training_phrases\": [%s], \"responses\": [%s]}",
                chat.getIntentName(),
                String.join(", ", Arrays.stream(trainingPhrasesArray).map(p -> "\"" + p.trim() + "\"").collect(Collectors.toList())),
                String.join(", ", Arrays.stream(responsesArray).map(r -> "\"" + r.trim() + "\"").collect(Collectors.toList()))
        );
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            chatDao.save(chat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/chat/list";
    }


    @PostMapping("/admin/chat/delete/{id}")
    public String deleteChat(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        Optional<Chat> chatOpt = chatDao.findById(id);
        if (chatOpt.isPresent()) {
            chatDao.delete(chatOpt.get());
            redirectAttributes.addFlashAttribute("successMessage", "Chat deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Chat not found.");
        }
        return "redirect:/admin/chat/list";

    }

}
