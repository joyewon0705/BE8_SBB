package com.mysite.sbb;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class SbbApplicationTests {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@BeforeEach
	void setup() {
		if (questionRepository.count() == 0) {
			Question q1 = new Question();
			q1.setSubject("sbb가 무엇인가요?");
			q1.setContent("sbb에 대해서 알고 싶습니다.");
			q1.setCreateDate(LocalDateTime.now());
			this.questionRepository.save(q1);

			Question q2 = new Question();
			q2.setSubject("스프링부트 모델 질문입니다.");
			q2.setContent("id는 자동으로 생성되나요?");
			q2.setCreateDate(LocalDateTime.now());
			this.questionRepository.save(q2);
		}
		if (answerRepository.count() == 0) {
			Optional<Question> oq = this.questionRepository.findById(2);
			assertTrue(oq.isPresent());
			Question q = oq.get();

			Answer a = new Answer();
			a.setContent("네 자동으로 생성됩니다.");
			a.setQuestion(q);
			a.setCreateDate(LocalDateTime.now());
			this.answerRepository.save(a);
		}
	}

	@Test
	@DisplayName("findAll")
	void t1() {
		List<Question> all = this.questionRepository.findAll();
		assertEquals(2, all.size());

		Question q = all.get(0);
		assertEquals("sbb가 무엇인가요?", q.getSubject());
	}

	@Test
	@DisplayName("findById")
	void t2() {
		Optional<Question> oq = this.questionRepository.findById(1);
		if(oq.isPresent()) {
			Question q = oq.get();
			assertEquals("sbb가 무엇인가요?", q.getSubject());
		}
	}

	@Test
	@DisplayName("findBySubject")
	void t3() {
		Question q = this.questionRepository.findBySubject("sbb가 무엇인가요?");
		assertEquals(1, q.getId());
	}

	@Test
	@DisplayName("findBySubjectAndContent")
	void t4() {
		Question q = this.questionRepository.findBySubjectAndContent(
				"sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
		assertEquals(1, q.getId());
	}

	@Test
	@DisplayName("findBySubjectLike")
	void t5() {
		List<Question> qList = this.questionRepository.findBySubjectLike("sbb%");
		Question q = qList.get(0);
		assertEquals("sbb가 무엇인가요?", q.getSubject());
	}

	@Test
	@DisplayName("질문 수정")
	void t6() {
		Optional<Question> oq = this.questionRepository.findById(1);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		q.setSubject("수정된 제목");
		this.questionRepository.save(q);

		Question foundQuestion = questionRepository.findBySubject("수정된 제목");
		assertThat(foundQuestion).isNotNull();
	}

	@Test
	@DisplayName("질문 삭제")
	void t7() {
		assertEquals(2, this.questionRepository.count());
		Optional<Question> oq = this.questionRepository.findById(1);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		this.questionRepository.delete(q);
		assertEquals(1, this.questionRepository.count());
	}

	@Test
	@DisplayName("답변 생성")
	@Transactional
	void t8() {
		Optional<Question> oq = this.questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();

		Answer a = new Answer();
		a.setContent("네 자동으로 생성됩니다.");
		a.setQuestion(q);
		a.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(a);
	}

	@Test
	@DisplayName("답변 생성 v2")
	@Transactional
	@Rollback(value = false)
	void t9() {
		Question question = questionRepository.findById(2).get();

		question.addAnswer("네 자동으로 생성됩니다.");

		assertThat(question.getAnswerList()).hasSize(2);
	}

	@Test
	@DisplayName("답변 조회")
	void t10() {
		Optional<Answer> oa = this.answerRepository.findById(1);
		assertTrue(oa.isPresent());
		Answer a = oa.get();
		assertEquals(2, a.getQuestion().getId());
	}

	@Test
	@DisplayName("특정 질문에 대한 답변 조회")
	@Transactional
	void t11() {
		Optional<Question> oq = this.questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();

		List<Answer> answerList = q.getAnswerList();

		assertEquals(1, answerList.size());
		assertEquals("네 자동으로 생성됩니다.", answerList.get(0).getContent());
	}
}
